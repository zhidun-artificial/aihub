package dev.langchain4j.ollama.spring;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;

public class AssistantBuilder {
    private final Properties properties;

    private final HttpClientBuilder httpClientBuilder;

    public AssistantBuilder(Properties properties,  HttpClientBuilder httpClientBuilder) {
        this.properties = properties;
        this.httpClientBuilder = httpClientBuilder;
    }

    public StreamingChatLanguageModel streamingModel(String llmModel) {

        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        return OllamaStreamingChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(chatModelProperties.getBaseUrl())
                .modelName(llmModel)
                .temperature(chatModelProperties.getTemperature())
                .topK(chatModelProperties.getTopK())
                .topP(chatModelProperties.getTopP())
                .repeatPenalty(chatModelProperties.getRepeatPenalty())
                .seed(chatModelProperties.getSeed())
                .numPredict(chatModelProperties.getNumPredict())
                .stop(chatModelProperties.getStop())
                .supportedCapabilities(chatModelProperties.getSupportedCapabilities())
                .timeout(chatModelProperties.getTimeout())
                .customHeaders(chatModelProperties.getCustomHeaders())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .build();
    }
}
