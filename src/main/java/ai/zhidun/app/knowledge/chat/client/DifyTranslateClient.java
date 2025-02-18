package ai.zhidun.app.knowledge.chat.client;

import ai.zhidun.app.knowledge.chat.client.DifyChatFlowClient.ResponseMode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.HashMap;
import java.util.Map;

@HttpExchange(url = "/v1", accept = MediaType.APPLICATION_JSON_VALUE)
public interface DifyTranslateClient {

    @Data
    @Builder
    final class Request {
        @Builder.Default
        private Map<String, String> inputs = new HashMap<>();

        @Builder.Default
        @JsonProperty("response_mode")
        private ResponseMode responseMode = ResponseMode.BLOCKING;

        private String user;

    }

    record CompletionData(Map<String, String> outputs) {

    }

    record CompletionResponse(
            String workflow_run_id,
            String task_id,
            CompletionData data) {

    }

    @PostExchange("/workflows/run")
    CompletionResponse doTranslate(@RequestBody Request request);

}
