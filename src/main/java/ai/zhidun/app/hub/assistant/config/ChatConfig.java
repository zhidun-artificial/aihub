package ai.zhidun.app.hub.assistant.config;

import dev.langchain4j.http.client.HttpClientBuilder;
import ai.zhidun.app.hub.assistant.AssistantBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(ModelProperties.class)
public class ChatConfig {

    @Bean
    public AssistantBuilder assistantBuilder(ModelProperties properties,
                                             @Qualifier("ollamaStreamingChatModelHttpClientBuilder")
                                             HttpClientBuilder httpClientBuilder) {
        return new AssistantBuilder(properties.getOllamaLlm(), httpClientBuilder);
    }

}
