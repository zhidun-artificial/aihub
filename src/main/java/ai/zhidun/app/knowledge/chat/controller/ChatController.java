package ai.zhidun.app.knowledge.chat.controller;


import ai.zhidun.app.knowledge.chat.client.DifyChatFlowClient.ChunkChatCompletionEvent;
import ai.zhidun.app.knowledge.chat.client.DifyConversationClient.Conversation;
import ai.zhidun.app.knowledge.chat.client.DifyConversationClient.ListResponse;
import ai.zhidun.app.knowledge.chat.client.DifyConversationClient.Message;
import ai.zhidun.app.knowledge.chat.client.DifyConversationClient.SortBy;
import ai.zhidun.app.knowledge.chat.service.ChatService;
import ai.zhidun.app.knowledge.common.Response;
import ai.zhidun.app.knowledge.common.Response.Empty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Tag(name = "对话", description = "对话相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    public record ChatFile(
            @Schema(description = "临时上传文件id 或者 文库中文档的id")
            String id,
            @Schema(description = "文件名")
            String fileName,
            @Schema(description = "文件url，这个必须传递")
            String url
    ) {

    }

    public record ChatRequest(
            @Schema(description = "请求正文")
            String query,
            @Schema(description = "会话id，如果有值表示追问，否则是创建新绘画", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            String conversationId,
            @Schema(description = "文库中的文档")
            List<ChatFile> documents,
            @Schema(description = "临时上传文件")
            List<ChatFile> tmpFiles) {

    }

    @PostMapping("/conversations")
    public Flux<ServerSentEvent<ChunkChatCompletionEvent>> newChat(@RequestBody ChatRequest request) {
        return service.chatMessages(request.query, request.conversationId, request.documents, request.tmpFiles);
    }

    @GetMapping("/conversations")
    public Response<ListResponse<Conversation>> listConversation(
            @RequestParam(required = false) String lastId,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false, defaultValue = "UPDATED_AT_DESC") SortBy sort) {
        return Response.ok(service.listConversation(lastId, limit, sort));
    }

    @GetMapping("/conversations/{id}/messages")
    public Response<ListResponse<Message>> listMessages(
            @PathVariable String id,
            @RequestParam(required = false) String firstId,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        return Response.ok(service.listMessages(id, firstId, limit));
    }

    @DeleteMapping("/conversations/{id}")
    public Response<Empty> deleteConversation(@PathVariable String id) {
        service.deleteConversation(id);
        return Response.ok();
    }

    public record RenameRequest(String newName) {

    }

    @PostMapping("/conversations/{id}/rename")
    public Response<Empty>  rename(
            @PathVariable String id,@RequestBody RenameRequest request) {
        service.renameConversation(id, request.newName());
        return Response.ok();
    }
}