package ai.zhidun.app.knowledge.chat.client;

import ai.zhidun.app.knowledge.chat.client.DifyChatFlowClient.File;
import ai.zhidun.app.knowledge.chat.client.DifyChatFlowClient.ResponseMode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public final class ChatRequest {
    private String query;

    @Builder.Default
    private Map<String, Object> inputs = new HashMap<>();

    @Builder.Default
    @JsonProperty("response_mode")
    private ResponseMode responseMode = ResponseMode.STREAMING;

    private String user;

    @JsonProperty("conversation_id")
    private String conversationId;

    @Builder.Default
    private List<File> files = new ArrayList<>();

    @Builder.Default
    @JsonProperty("auto_generate_name")
    private boolean autoGenerateName = true;
}
