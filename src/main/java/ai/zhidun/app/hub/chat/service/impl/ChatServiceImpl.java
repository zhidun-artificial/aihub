package ai.zhidun.app.hub.chat.service.impl;

import ai.zhidun.app.hub.assistant.AssistantApi;
import ai.zhidun.app.hub.assistant.FileContent;
import ai.zhidun.app.hub.assistant.service.AssistantService;
import ai.zhidun.app.hub.auth.service.JwtSupport;
import ai.zhidun.app.hub.auth.service.UserService;
import ai.zhidun.app.hub.chat.controller.ChatController.SearchConversation;
import ai.zhidun.app.hub.chat.controller.ChatController.SearchMessage;
import ai.zhidun.app.hub.chat.dao.Conversation;
import ai.zhidun.app.hub.chat.dao.ConversationMapper;
import ai.zhidun.app.hub.chat.model.ConversationVo;
import ai.zhidun.app.hub.chat.model.MessageVo;
import ai.zhidun.app.hub.chat.model.QueryContext;
import ai.zhidun.app.hub.chat.model.RetrievalResource;
import ai.zhidun.app.hub.chat.service.ChatEvent.FinishedEvent;
import ai.zhidun.app.hub.chat.service.ChatEvent.PartialMessageEvent;
import ai.zhidun.app.hub.chat.service.ChatEvent.RetrievedContentEvent;
import ai.zhidun.app.hub.chat.service.ChatService;
import ai.zhidun.app.hub.common.BizError;
import ai.zhidun.app.hub.common.BizException;
import ai.zhidun.app.hub.tmpfile.service.UploadResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.TokenStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class ChatServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ChatService {

    public static final String GLOBAL_DEFAULT_ASSISTANT = "global_default_assistant";

    private final MessageService messageService;

    private final AssistantService assistantService;

    private final UserService userService;

    public ChatServiceImpl(MessageService messageService,
                           AssistantService assistantService,
                           UserService userService) {
        this.messageService = messageService;
        this.assistantService = assistantService;
        this.userService = userService;
    }

    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();

    private SseEmitter doChat(AssistantApi api, String conversationId, String query) {
        SseEmitter emitter = new SseEmitter();

        TokenStream stream = api.chat(conversationId, query)
                .onRetrieved(contents -> {
                    try {
                        QueryContext ctx = into(contents);
                        messageService.newMessage(conversationId, query, ctx);
                        emitter.send(new RetrievedContentEvent(conversationId, ctx), MediaType.APPLICATION_JSON);
                    } catch (IOException e) {
                        log.warn("Failed to send contents", e);
                    }
                })
                .onPartialResponse(text -> {
                    try {
                        emitter.send(new PartialMessageEvent(conversationId,text), MediaType.APPLICATION_JSON);
                    } catch (IOException e) {
                        log.warn("Failed to send partial response", e);
                    }
                })
                .onCompleteResponse((response) -> {
                    try {
                        messageService.finishMessage(conversationId, response.aiMessage().text());
                        emitter.send(new FinishedEvent(conversationId, response.metadata().toString()), MediaType.APPLICATION_JSON);
                    } catch (IOException e) {
                        log.warn("Failed to send partial response", e);
                    } finally {
                        log.info("Chat completed");
                        emitter.complete();
                    }
                })
                //todo may define some error event
                .onError(emitter::completeWithError);

        executor.execute(stream::start);
        return emitter;
    }

    private QueryContext into(List<Content> contents) {
        List<RetrievalResource> resources = new ArrayList<>();

        List<UploadResult> files = new ArrayList<>();
        for (Content content : contents) {
            if (content instanceof FileContent file) {
                files.add(file.result());
            } else {
                // todo may need more field
                Metadata metadata = content.textSegment().metadata();
                String documentId = metadata.getString("documentId");
                String url = metadata.getString("url");
                String fileName = metadata.getString("fileName");
                resources.add(new RetrievalResource(documentId, fileName, url));
            }
        }
        return new QueryContext(resources, files);
    }

    private void ensureDefaultAssistant(String conversationId) {
        if (this.getById(conversationId) instanceof Conversation conversation) {
            if (!GLOBAL_DEFAULT_ASSISTANT.equals(conversation.getAssistantId())) {
                throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("会话[" + conversationId + "]不是默认助手!"));
            }
        } else {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("会话[" + conversationId + "]不存在!"));
        }
    }

    private void ensureConversationExists(String conversationId, String assistantId) {
        if (this.getById(conversationId) instanceof Conversation conversation) {
            if (!Objects.equals(assistantId, conversation.getAssistantId())) {
                throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("会话[" + conversationId + "]和助手[" + assistantId+"]不匹配!"));
            }
        } else {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("会话[" + conversationId + "]不存在!"));
        }
    }

    @Override
    public SseEmitter chat(ChatParam param) {
        String conversationId;

        if (StringUtils.isNotBlank(param.conversationId())) {
            ensureDefaultAssistant(param.conversationId());
            conversationId = param.conversationId();
        } else {
            Conversation entity = new Conversation();
            entity.setName("新建会话");
            entity.setAssistantId(GLOBAL_DEFAULT_ASSISTANT);
            entity.setCreator(JwtSupport.userId());
            this.save(entity);
            conversationId = entity.getId();
        }

        AssistantApi api = assistantService.buildApi(param.llmModel(),
            "你是一个很厉害的助手，尽量使用中文回答",
            param.baseIds(),
            param.files());

        return doChat(api, conversationId, param.query());
    }

    @Override
    public SseEmitter chat(AssistantChatParam param) {
        String conversationId;

        if (StringUtils.isNotBlank(param.conversationId())) {
            ensureConversationExists(param.conversationId(), param.assistantId());
            conversationId = param.conversationId();
        } else {
            Conversation entity = new Conversation();
            entity.setName("新建会话");
            entity.setAssistantId(param.assistantId());
            entity.setCreator(JwtSupport.userId());
            this.save(entity);
            conversationId = entity.getId();
        }

        AssistantApi api = assistantService.buildApi(param.assistantId(), param.files());

        return doChat(api, conversationId, param.query());
    }

    @Override
    public IPage<ConversationVo> conversations(SearchConversation request) {
        PageDTO<Conversation> page = new PageDTO<>(request.pageNo(), request.pageSize());

        LambdaQueryWrapper<Conversation> query = Wrappers
                .lambdaQuery(Conversation.class)
                .like(StringUtils.isNotBlank(request.key()), Conversation::getName, "%" + request.key() + "%");

        query = request
                .sort()
                .sort(query, Conversation::getCreateTime, Conversation::getUpdateTime);

        IPage<Conversation> result = this
                .page(page, query);

        return result.convert(this::from);
    }

    @SneakyThrows
    public ConversationVo from(Conversation entity) {
        String creatorName = userService.name(entity.getCreator());
        return new ConversationVo(
                entity.getId(),
                entity.getName(),
                entity.getCreator(),
                creatorName,
                entity.getCreateTime().getTime(),
                entity.getUpdateTime().getTime());
    }

    @Override
    public IPage<MessageVo> messages(SearchMessage request) {
        return messageService.messages(request);
    }
}
