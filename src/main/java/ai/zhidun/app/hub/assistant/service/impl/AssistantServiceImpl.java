package ai.zhidun.app.hub.assistant.service.impl;

import ai.zhidun.app.hub.assistant.AssistantApi;
import ai.zhidun.app.hub.assistant.AssistantBuilder;
import ai.zhidun.app.hub.assistant.FileContent;
import ai.zhidun.app.hub.assistant.controller.AssistantController;
import ai.zhidun.app.hub.assistant.dao.Assistant;
import ai.zhidun.app.hub.assistant.dao.AssistantBaseMap;
import ai.zhidun.app.hub.assistant.dao.AssistantBaseMapMapper;
import ai.zhidun.app.hub.assistant.dao.AssistantMapper;
import ai.zhidun.app.hub.assistant.model.AssistantDetailVo;
import ai.zhidun.app.hub.assistant.model.AssistantVo;
import ai.zhidun.app.hub.assistant.service.AssistantService;
import ai.zhidun.app.hub.auth.service.AuthSupport;
import ai.zhidun.app.hub.auth.service.UserGroupService;
import ai.zhidun.app.hub.auth.service.UserService;
import ai.zhidun.app.hub.common.BizError;
import ai.zhidun.app.hub.common.BizException;
import ai.zhidun.app.hub.common.PermitConst;
import ai.zhidun.app.hub.documents.langchain4j.EmbeddingStoresContentRetriever;
import ai.zhidun.app.hub.documents.service.KnowledgeBaseService;
import ai.zhidun.app.hub.tmpfile.service.UploadResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AssistantServiceImpl extends ServiceImpl<AssistantMapper, Assistant> implements AssistantService {

    private final AssistantBaseMapMapper mapMapper;

    private final AssistantBuilder assistantBuilder;

    private final KnowledgeBaseService baseService;

    private final ApplicationEventPublisher publisher;

    private final UserService userService;

    private final UserGroupService groupService;

    public AssistantServiceImpl(AssistantBaseMapMapper mapMapper,
                                AssistantBuilder assistantBuilder,
                                KnowledgeBaseService baseService,
                                ApplicationEventPublisher publisher,
                                UserService userService,
                                UserGroupService groupService) {
        this.mapMapper = mapMapper;
        this.assistantBuilder = assistantBuilder;
        this.baseService = baseService;
        this.publisher = publisher;
        this.userService = userService;
        this.groupService = groupService;
    }

    @Override
    public AssistantVo create(AssistantCreateParam param) {
        if (this.lambdaQuery().eq(Assistant::getName, param.name()).exists()) {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("助手名称已存在!"));
        }

        Assistant entity = new Assistant();
        entity.setName(param.name());
        entity.setLlmModel(param.llmModel());
        entity.setExt(param.ext().toPrettyString());
        entity.setDescription(param.description());
        entity.setCreator(AuthSupport.userId());
        entity.setPermit(param.permit());
        entity.setGroupId(param.groupId());
        entity.setSystemPrompt(param.systemPrompt());
        this.save(entity);

        if (param.baseIds() instanceof List<String> ids && !ids.isEmpty()) {
            saveBaseIds(ids, entity.getId());
        }

        entity = this.getById(entity.getId());

        return from(entity);
    }

    private void saveBaseIds(List<String> ids, String id) {
        List<AssistantBaseMap> maps = new ArrayList<>();
        for (String baseId : ids) {
            AssistantBaseMap e = new AssistantBaseMap();
            e.setBaseId(baseId);
            e.setAssistantId(id);
            maps.add(e);
        }
        mapMapper.insertIgnoreBatchSomeColumn4Mysql(maps);
    }

    @Override
    public AssistantVo update(AssistantUpdateParam param) {
        Assistant entity = this.getById(param.id());
        if (entity.getExt() != null) {
            entity.setExt(param.ext().toPrettyString());
        }
        if (entity.getName() != null) {
            entity.setName(param.name());
        }
        if (entity.getDescription() != null) {
            entity.setDescription(param.description());
        }
        if (entity.getLlmModel() != null) {
            entity.setLlmModel(param.llmModel());
        }
        this.updateById(entity);

        if (param.baseIds() instanceof List<String> ids && !ids.isEmpty()) {
            mapMapper.delete(Wrappers
                    .lambdaQuery(AssistantBaseMap.class)
                    .eq(AssistantBaseMap::getAssistantId, param.id())
            );
            saveBaseIds(ids, entity.getId());
        }

        return this.from(entity);
    }

    private final JsonMapper mapper = new JsonMapper();

    @SneakyThrows
    public AssistantVo from(Assistant entity) {
        String creatorName = userService.name(entity.getCreator());
        return new AssistantVo(
                entity.getId(),
                entity.getName(),
                entity.getLlmModel(),
                entity.getSystemPrompt(),
                entity.getPermit(),
                entity.getGroupId(),
                entity.getDescription(),
                entity.getCreator(),
                creatorName,
                mapper.readTree(entity.getExt()),
                entity.getCreateTime().getTime(),
                entity.getUpdateTime().getTime());
    }

    public record AssistantDeleteEvent(String id) {
    }

    @Override
    public void delete(String id) {
        this.removeById(id);
        publisher.publishEvent(new AssistantDeleteEvent(id));
    }

    @Override
    public IPage<AssistantVo> search(AssistantController.SearchAssistant request) {
        PageDTO<Assistant> page = new PageDTO<>(request.pageNo(), request.pageSize());

        LambdaQueryWrapper<Assistant> query = Wrappers
                .lambdaQuery(Assistant.class)
                .like(StringUtils.isNotBlank(request.key()), Assistant::getName, "%" + request.key() + "%");

        if (!AuthSupport.superAdmin()) {
            if (!request.forEdit()) {
                List<String> groupIds = groupService.groupIdsBy(AuthSupport.userId());
                query = query
                        .and(l -> l
                                .and(c -> c
                                        .eq(Assistant::getPermit, PermitConst.PUBLIC_RESOURCE)
                                )
                                .or(c -> c
                                        .eq(Assistant::getPermit, PermitConst.PERSONAL_RESOURCE)
                                        .eq(Assistant::getCreator, AuthSupport.userId())
                                )
                                .or(c -> c
                                        .eq(Assistant::getPermit, PermitConst.GROUP_RESOURCE)
                                        .in(!groupIds.isEmpty(), Assistant::getGroupId, groupIds)
                                )
                        );
            } else {
                List<String> groupIds = groupService.groupIdsAdminBy(AuthSupport.userId());
                if (groupIds.isEmpty()) {
                    return page.convert(this::from);
                } else {
                    query = query.in(Assistant::getGroupId, groupIds);
                }
            }
        } else {
            log.debug("super admin do not filter group");
        }

        query = request
                .sort()
                .sort(query, Assistant::getCreateTime, Assistant::getUpdateTime);

        IPage<Assistant> result = this
                .page(page, query);

        return result.convert(this::from);
    }

    @Override
    public AssistantDetailVo detail(String id) {
        Assistant assistant = this.getById(id);

        List<String> baseIds = mapMapper.selectList(Wrappers
                        .lambdaQuery(AssistantBaseMap.class)
                        .eq(AssistantBaseMap::getAssistantId, id))
                .stream()
                .map(AssistantBaseMap::getBaseId)
                .toList();

        List<KnowledgeBaseService.BaseInfo> infos = baseService.listBaseInfo(baseIds);


        return new AssistantDetailVo(from(assistant), infos);
    }

    private List<Content> loadFiles(List<UploadResult> files) {
        if (files == null) {
            return List.of();
        }
        List<Content> contents = new ArrayList<>();
        for (UploadResult file : files) {
            // 不在分片，整个文件加载
            contents.add(FileContent.from(file));
        }
        return contents;
    }

    //todo maybe cache it for performance later
    @Override
    public AssistantApi buildApi(String id, List<UploadResult> files) {
        if (this.getById(id) instanceof Assistant assistant) {
            LambdaQueryWrapper<AssistantBaseMap> query = Wrappers
                    .lambdaQuery(AssistantBaseMap.class)
                    .eq(AssistantBaseMap::getAssistantId, id);

            List<String> baseIds = mapMapper.selectList(query)
                    .stream()
                    .map(AssistantBaseMap::getBaseId)
                    .toList();

            return buildApi(assistant.getLlmModel(), assistant.getSystemPrompt(), baseIds, files);
        } else {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("助手不存在!"));
        }
    }

    // todo make the memory persistent
    // https://github.com/langchain4j/langchain4j-examples/blob/main/other-examples/src/main/java/ServiceWithPersistentMemoryForEachUserExample.java#L59
    private final InMemoryChatMemoryStore chatMemoryStore = new InMemoryChatMemoryStore();

    //never cache
    @Override
    public AssistantApi buildApi(String llmModel, String systemPrompt, List<String> baseIds, List<UploadResult> files) {
        AiServices<AssistantApi> builder = AiServices
                .builder(AssistantApi.class)
                .streamingChatLanguageModel(assistantBuilder.streamingModel(llmModel))
                // here memory is conversation id
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory
                        .builder()
                        //todo make it configurable
                        .maxMessages(10)
                        .chatMemoryStore(chatMemoryStore)
                        .build());

        if (baseIds instanceof List<String> ids && !ids.isEmpty()) {
            List<EmbeddingStoreContentRetriever> stores = new ArrayList<>();
            for (String id : ids) {
                EmbeddingStore<TextSegment> store = baseService.embeddingStore(id);
                stores.add(EmbeddingStoreContentRetriever.builder()
                        .embeddingStore(store)
                        .embeddingModel(baseService.embeddingModel(id))
                        .build());
            }

            return builder
                    .systemMessageProvider(memoryId -> systemPrompt)
                    .contentRetriever(new EmbeddingStoresContentRetriever(loadFiles(files), stores))
                    .build();
        } else {
            return builder.build();
        }
    }


}
