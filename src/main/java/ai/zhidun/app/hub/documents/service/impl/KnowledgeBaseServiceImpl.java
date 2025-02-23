package ai.zhidun.app.hub.documents.service.impl;

import ai.zhidun.app.hub.auth.service.JwtSupport;
import ai.zhidun.app.hub.auth.service.UserService;
import ai.zhidun.app.hub.documents.controller.KnowledgeBaseController;
import ai.zhidun.app.hub.documents.dao.*;
import ai.zhidun.app.hub.documents.model.KnowledgeBaseVo;
import ai.zhidun.app.hub.documents.service.KnowledgeBaseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.SneakyThrows;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {

    private final DocumentAggMapper documentAggMapper;

    private final BaseTagMapper tagMapper;

    public final UserService userService;

    public KnowledgeBaseServiceImpl(DocumentAggMapper documentAggMapper, BaseTagMapper tagMapper, UserService userService) {
        this.documentAggMapper = documentAggMapper;
        this.tagMapper = tagMapper;
        this.userService = userService;
    }

    public KnowledgeBaseVo from(KnowledgeBase entity) {
        return from(entity, null);
    }

    private final JsonMapper mapper = new JsonMapper();

    @SneakyThrows
    public KnowledgeBaseVo from(KnowledgeBase entity, Integer docCount) {
        String creatorName = userService.name(entity.getCreator());
        return new KnowledgeBaseVo(
                entity.getId(),
                entity.getName(),
                entity.getCreator(),
                creatorName,
                docCount,
                mapper.readTree(entity.getExt()),
                entity.getCreateTime().getTime(),
                entity.getUpdateTime().getTime());
    }

    @Override
    @Transactional
    public KnowledgeBaseVo create(CreateKnowledgeBase create) {
        // todo create vector store

        KnowledgeBase entity = new KnowledgeBase();
        entity.setName(create.name());
        entity.setEmbedModel(create.embedModel());
        entity.setDescription(create.description());
        entity.setExt(create.ext().toPrettyString());
        entity.setCreator(JwtSupport.userId());
        this.save(entity);

        if (create.tags() instanceof List<String> list && !list.isEmpty()) {
            saveTags(list, entity.getId());
        }


        entity = this.getById(entity.getId());
        return this.from(entity);
    }

    @Override
    @Transactional
    public KnowledgeBaseVo update(UpdateKnowledgeBase update) {
        // todo update vector store

        KnowledgeBase entity = this.getById(update.id());
        if (entity.getName() != null) {
            entity.setName(update.name());
        }
        if (entity.getEmbedModel() != null) {
            entity.setEmbedModel(update.embedModel());
        }
        if (entity.getDescription() != null) {
            entity.setDescription(update.description());
        }
        if (entity.getExt() != null) {
            entity.setExt(update.ext().toPrettyString());
        }
        this.updateById(entity);

        if (update.tags() instanceof List<String> list && !list.isEmpty()) {
            tagMapper.delete(Wrappers
                    .lambdaQuery(BaseTag.class)
                    .eq(BaseTag::getBaseId, update.id()));

            saveTags(list, entity.getId());
        }

        return this.from(entity);
    }

    private void saveTags(List<String> list, String baseId) {
        List<BaseTag> tags = new ArrayList<>();
        for (String tag : list) {
            BaseTag e = new BaseTag();
            e.setBaseId(baseId);
            e.setTag(tag);
            tags.add(e);
        }
        tagMapper.insertIgnoreBatchSomeColumn4Mysql(tags);
    }

    @Override
    public void delete(String id) {
        this.removeById(id);
        // todo cascade delete
    }

    @Override
    public IPage<KnowledgeBaseVo> search(KnowledgeBaseController.SearchKnowledgeBase request) {
        PageDTO<KnowledgeBase> page = new PageDTO<>(request.pageNo(), request.pageSize());

        LambdaQueryWrapper<KnowledgeBase> query = Wrappers
                .lambdaQuery(KnowledgeBase.class)
                .like(StringUtils.isNotBlank(request.key()), KnowledgeBase::getName, "%" + request.key() + "%");

        query = request
                .sort()
                .sort(query, KnowledgeBase::getCreateTime, KnowledgeBase::getUpdateTime);

        IPage<KnowledgeBase> result = this
                .page(page, query);

        List<String> baseIds = new ArrayList<>();
        for (KnowledgeBase base : result.getRecords()) {
            baseIds.add(base.getId());
        }

        Map<String, Integer> countMap = new HashMap<>();

        if (!baseIds.isEmpty()) {
            LambdaQueryWrapper<DocumentAgg> wrapper = Wrappers
                    .lambdaQuery(DocumentAgg.class)
                    .select(DocumentAgg::getBaseId, DocumentAgg::getCount)
                    .in(DocumentAgg::getBaseId, baseIds)
                    .groupBy(DocumentAgg::getBaseId);

            for (DocumentAgg documentAgg : documentAggMapper.selectList(wrapper)) {
                countMap.put(documentAgg.getBaseId(), documentAgg.getCount());
            }
        }

        return result.convert(base ->  this.from(base, countMap.getOrDefault(base.getId(), 0)));
    }

    @Override
    public Optional<KnowledgeBaseVo> getFistByName(String libraryName) {

        LambdaQueryWrapper<KnowledgeBase> query = Wrappers
                .lambdaQuery(KnowledgeBase.class)
                .eq(KnowledgeBase::getName, libraryName);

        List<KnowledgeBase> list = this.list(query);
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(this.from(list.getFirst()));
        }
    }

    @Override
    public EmbeddingStore<TextSegment> embeddingStore(String id) {
        //todo
        throw new NotImplementedException();
    }

    @Override
    public List<BaseInfo> listBaseInfo(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return this.lambdaQuery()
                .in(KnowledgeBase::getId, ids)
                .select(KnowledgeBase::getId, KnowledgeBase::getName)
                .list()
                .stream()
                .map(e -> new BaseInfo(e.getId(), e.getName()))
                .toList();
    }
}
