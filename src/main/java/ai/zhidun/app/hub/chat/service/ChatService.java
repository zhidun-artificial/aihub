package ai.zhidun.app.hub.chat.service;

import ai.zhidun.app.hub.chat.controller.ChatController;
import ai.zhidun.app.hub.chat.model.ConversationVo;
import ai.zhidun.app.hub.chat.model.MessageVo;
import ai.zhidun.app.hub.tmpfile.service.UploadResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface ChatService {

    record ChatParam(
            String conversationId,
            String query,
            String llmModel,
            List<String> baseIds,
            List<UploadResult> files
    ) {
    }

    record AssistantChatParam(
            String conversationId,
            String query,
            String assistantId,
            List<UploadResult> files
    ) {
    }

    SseEmitter chat(ChatParam param);

    SseEmitter chat(AssistantChatParam param);

    void cancel(String messageId, String text);

    IPage<ConversationVo> conversations(ChatController.SearchConversation request);

    IPage<MessageVo> messages(ChatController.SearchMessage request);
}
