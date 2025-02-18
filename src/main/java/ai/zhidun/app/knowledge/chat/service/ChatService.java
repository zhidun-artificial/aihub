package ai.zhidun.app.knowledge.chat.service;

import ai.zhidun.app.knowledge.chat.client.ChatRequest;
import ai.zhidun.app.knowledge.chat.client.DifyChatFlowClient;
import ai.zhidun.app.knowledge.chat.client.DifyChatFlowClient.ChunkChatCompletionEvent;
import ai.zhidun.app.knowledge.chat.client.DifyChatFlowClient.File;
import ai.zhidun.app.knowledge.chat.client.DifyConversationClient;
import ai.zhidun.app.knowledge.chat.client.DifyConversationClient.*;
import ai.zhidun.app.knowledge.chat.controller.ChatController.ChatFile;
import ai.zhidun.app.knowledge.chat.model.OpLogVo;
import ai.zhidun.app.knowledge.security.auth.service.JwtService.AuthedClaimInfo;
import ai.zhidun.app.knowledge.security.auth.service.JwtSupport;
import ai.zhidun.app.knowledge.store.service.S3Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ai.zhidun.app.knowledge.chat.client.DifyChatFlowClient.ResponseMode.STREAMING;

@Slf4j
@Service
public class ChatService {
    public static final String FILES_KEY = "files";
    private final DifyChatFlowClient chatFlowClient;
    private final DifyConversationClient conversationClient;

    private final S3Service s3Service;

    private final OpLogService opLogService;

    private final Boolean customRename;

    public ChatService(DifyChatFlowClient chatFlowClient,
                       DifyConversationClient conversationClient,
                       S3Service s3Service,
                       OpLogService opLogService,
                       @Value("${dify.rename-custom}") Boolean customRename) {
        this.chatFlowClient = chatFlowClient;
        this.conversationClient = conversationClient;
        this.s3Service = s3Service;
        this.opLogService = opLogService;
        this.customRename = customRename;
    }

    private final ExecutorService taskExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public Flux<ServerSentEvent<ChunkChatCompletionEvent>> chatMessages(
            String query, String conversationId,
            List<ChatFile> documents, List<ChatFile> tmpFiles) {

        List<File> files = getFiles(documents, tmpFiles);

        String user = currentUser();

        String userName = currentUserName();

        ChatRequest request = ChatRequest
                .builder()
                .query(query)
                .inputs(Map.of(FILES_KEY, files))
                .conversationId(conversationId)
                .user(user)
                .autoGenerateName(false)
                .responseMode(STREAMING)
                .build();

        if (log.isDebugEnabled()) {
            JsonMapper mapper = new JsonMapper();
            try {
                log.debug("chatMessages request: {}", mapper.writeValueAsString(request));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        final String[] newConversationId = new String[1];
        final int userId = JwtSupport.userId();

        return chatFlowClient
                .chat(request)
                .doOnNext(sse -> {
                    if (sse.data() instanceof ChunkChatCompletionEvent event) {
                        switch (event) {
                            case DifyChatFlowClient.ChunkChatErrorEvent ignored -> {
                            }
                            case DifyChatFlowClient.ChunkChatMessageEndEvent chunkChatMessageEndEvent ->
                                    newConversationId[0] = chunkChatMessageEndEvent.conversationId();
                            case DifyChatFlowClient.ChunkChatMessageEvent chunkChatMessageEvent ->
                                    newConversationId[0] = chunkChatMessageEvent.conversationId();
                            case DifyChatFlowClient.ChunkChatMessageFileEvent chunkChatMessageFileEvent ->
                                    newConversationId[0] = chunkChatMessageFileEvent.conversationId();
                            case DifyChatFlowClient.ChunkChatMessageReplaceEvent chunkChatMessageReplaceEvent ->
                                    newConversationId[0] = chunkChatMessageReplaceEvent.conversationId();
                            case DifyChatFlowClient.ChunkChatNodeFinishedEvent ignored -> {
                            }
                            case DifyChatFlowClient.ChunkChatNodeStartedEvent ignored -> {
                            }
                            case DifyChatFlowClient.ChunkChatWorkflowFinishedEvent ignored -> {
                            }
                            case DifyChatFlowClient.ChunkChatWorkflowStartedEvent ignored -> {
                            }
                            case DifyChatFlowClient.ChunkTTSMessageEndEvent ignored -> {
                            }
                            case DifyChatFlowClient.ChunkTTSMessageEvent ignored -> {
                            }
                            case DifyChatFlowClient.Other ignored -> {
                            }
                        }

                    }
                })
                .doFinally(signalType -> taskExecutor.submit(() -> {
                    if (StringUtils.hasText(conversationId)) {
                        // 这里更新时间调整
                        opLogService.addMessageCount(conversationId);
                    } else if (newConversationId[0] != null) {
                        // 这里插入记录
                        try {
                            RenameRequest renameRequest = renameRequest(user, query, userName);
                            RenameResponse response = conversationClient.renameConversation(newConversationId[0], renameRequest);
                            OpLogVo vo = opLogService.newOpLog(newConversationId[0], response.name(), userId);
                            if (log.isDebugEnabled()) {
                                log.debug("{} saved", vo);
                            }
                            log.info("conversation {} renamed {} ", newConversationId[0], response.name());
                        } catch (RuntimeException e) {
                            log.warn("save op log failed!", e);
                        }
                    }
                }));
    }

    private static  final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss X");

    private static String currentTime() {
        Instant instant = Instant.now().atZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).toInstant();
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Shanghai"));
        return zonedDateTime.format(FORMATTER);
    }

    private RenameRequest renameRequest(String user, String query, String userName) {
        if (customRename) {
            String trimQuery;
            if (query.length() > 10) {
                trimQuery = query.substring(0, 10);
            } else {
                trimQuery = query;
            }
            String name = trimQuery + "_" + userName + "_" + currentTime();
            return new RenameRequest(name, false, user);
        } else {
            return new RenameRequest(null, true, user);
        }
    }
    private static String currentUserName() {
        AuthedClaimInfo info = JwtSupport.claimInfo().orElseThrow();
        return info.name();
    }

    private static String currentUser() {
        AuthedClaimInfo info = JwtSupport.claimInfo().orElseThrow();
        return "knowledge-nexus-" + info.userId() + "-" + info.name();
    }

    private List<File> getFiles(List<ChatFile> documents, List<ChatFile> tmpFiles) {
        List<File> files = new ArrayList<>();
        if (documents instanceof List<ChatFile> chatFiles) {
            for (ChatFile chatFile : chatFiles) {
                String url = s3Service.localUrl(chatFile.url());
                files.add(new DifyChatFlowClient.RemoteUrl("document", url));
            }
        }
        if (tmpFiles instanceof List<ChatFile> chatFiles) {
            for (ChatFile chatFile : chatFiles) {
                String url = s3Service.localUrl(chatFile.url());
                files.add(new DifyChatFlowClient.RemoteUrl("document", url));
            }
        }
        return files;
    }

    public ListResponse<Conversation> listConversation(String lastId, Integer limit, SortBy sort) {
        String user = currentUser();
        ListResponse<Conversation> response = conversationClient.listConversation(user, lastId, limit, sort.getSort());
        for (Conversation datum : response.data()) {
            updateInputs(datum.inputs());
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    void updateInputs(Map<String, Object> inputs) {
        if (inputs.get(FILES_KEY) instanceof List<?> array) {
            for (Object node : array) {
                Map<String, Object> map = (Map<String, Object>) node;
                if (map.containsKey("remote_url")) {
                    String url = map.get("remote_url").toString();
                    map.put("remote_url", s3Service.publicUrl(url));
                }
            }
        }
    }

    public ListResponse<Message> listMessages(String conversationId, String firstId, Integer limit) {
        String user = currentUser();
        ListResponse<Message> response = conversationClient.listMessages(conversationId, user, firstId, limit);
        for (Message datum : response.data()) {
            updateInputs(datum.inputs());
        }
        return response;
    }

    public void renameConversation(String id, String name) {
        conversationClient.renameConversation(id, new RenameRequest(name, false, currentUser()));
        opLogService.rename(id, name);
    }

    public void deleteConversation(String id) {
        DeleteRequest request = new DeleteRequest(currentUser());
        conversationClient.deleteConversation(id, request);

    }
}
