package ai.zhidun.app.knowledge.chat.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(url = "/api", accept = MediaType.APPLICATION_JSON_VALUE)
public interface OllamaClient {

    @Data
    @Builder
    final class Request {
        private String model;
        private String system;
        private String prompt;

        @Builder.Default
        private boolean stream = false;
    }

    record CompletionResponse(
            String model,
            @JsonAlias("created_at")
            String creatAt,
            String response,
            boolean done,
            int[] context,
            @JsonAlias("total_duration")
            long totalDuration,
            @JsonAlias("load_duration")
            long loadDuration,
            @JsonAlias("prompt_eval_count")
            long promptEvalCount,
            @JsonAlias("prompt_eval_duration")
            long promptEvalDuration,
            @JsonAlias("eval_count")
            long evalCount,
            @JsonAlias("eval_duration")
            long evalDuration) {

    }

    @PostExchange("/generate")
    CompletionResponse completion(@RequestBody Request request);

}
