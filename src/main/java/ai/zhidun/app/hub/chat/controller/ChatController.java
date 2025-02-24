package ai.zhidun.app.hub.chat.controller;

import ai.zhidun.app.hub.chat.model.ConversationVo;
import ai.zhidun.app.hub.chat.model.MessageVo;
import ai.zhidun.app.hub.chat.service.ChatEvent;
import ai.zhidun.app.hub.chat.service.ChatService;
import ai.zhidun.app.hub.chat.service.ChatService.AssistantChatParam;
import ai.zhidun.app.hub.chat.service.ChatService.ChatParam;
import ai.zhidun.app.hub.common.BizError;
import ai.zhidun.app.hub.common.Response;
import ai.zhidun.app.hub.common.Response.PageVo;
import ai.zhidun.app.hub.common.Sort;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@ApiResponses(value = {
    @ApiResponse(
        responseCode = "400",
        description = "BAD Request",
        content = @Content(
            schema = @Schema(implementation = BizError.class),
            mediaType = MediaType.APPLICATION_JSON_VALUE
        )
    )
})
@Tag(name = "对话管理", description = "对话相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    public record SearchConversation(
            @Schema(description = "按名字检索")
            String key,
            @Schema(defaultValue = "1", description = "从1开始")
            Integer pageNo,
            @Schema(defaultValue = "20")
            Integer pageSize,
            Sort sort) {

        public SearchConversation(String key, Integer pageNo, Integer pageSize, Sort sort) {
            this.key = key;
            this.pageNo = pageNo != null ? pageNo : 1;
            this.pageSize = pageSize != null ? pageSize : 20;
            this.sort = sort != null ? sort : Sort.CREATED_AT_DESC;
        }
    }

    @PostMapping("/conversations/search")
    public Response<PageVo<ConversationVo>> search(@RequestBody SearchConversation request) {
        return Response.page(service.conversations(request));
    }

    public record SearchMessage(
            String conversationId,
            @Schema(defaultValue = "1", description = "从1开始")
            Integer pageNo,
            @Schema(defaultValue = "20")
            Integer pageSize,
            Sort sort) {

        public SearchMessage(String conversationId, Integer pageNo, Integer pageSize, Sort sort) {
            this.conversationId = conversationId;
            this.pageNo = pageNo != null ? pageNo : 1;
            this.pageSize = pageSize != null ? pageSize : 20;
            this.sort = sort != null ? sort : Sort.CREATED_AT_DESC;
        }
    }

    @PostMapping("/conversations/messages/search")
    public Response<PageVo<MessageVo>> messages(@RequestBody SearchMessage request) {
        return Response.page(service.messages(request));
    }

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                        mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                        schema =@Schema(ref = "_ChatEvent_")
                    )
            )
    })
    @PostMapping(value = "/conversations/chat")
    public SseEmitter newChat(@RequestBody ChatParam param) {
        return service.chat(param);
    }

  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
              schema =@Schema(ref = "_ChatEvent_")
          )
      )
  })
    @PostMapping(value = "/conversations/chat_with_assistant")
    public SseEmitter newChat(@RequestBody AssistantChatParam param) {
        return service.chat(param);
    }
}