package ai.zhidun.app.hub.assistant;

import ai.zhidun.app.hub.assistant.config.OllamaLlmProperties;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;

public class AssistantBuilder {
    private final OllamaLlmProperties properties;

    private final HttpClientBuilder httpClientBuilder;

    public AssistantBuilder(OllamaLlmProperties properties,  HttpClientBuilder httpClientBuilder) {
        this.properties = properties;
        this.httpClientBuilder = httpClientBuilder;
    }

    public StreamingChatLanguageModel streamingModel(String llmModel) {

        String modelName = llmModel != null ? llmModel : properties.getModelName();

        return OllamaStreamingChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(properties.getBaseUrl())
                .modelName(modelName)
                .temperature(properties.getTemperature())
                .topK(properties.getTopK())
                .topP(properties.getTopP())
                .repeatPenalty(properties.getRepeatPenalty())
                .seed(properties.getSeed())
                .numPredict(properties.getNumPredict())
                .stop(properties.getStop())
                .timeout(properties.getTimeout())
                .logRequests(properties.getLogRequests())
                .logResponses(properties.getLogResponses())
                .build();
    }
}
