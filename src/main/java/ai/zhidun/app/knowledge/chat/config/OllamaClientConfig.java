package ai.zhidun.app.knowledge.chat.config;

import ai.zhidun.app.knowledge.chat.client.OllamaClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@EnableConfigurationProperties({OllamaProperties.class})
public class OllamaClientConfig {

    @Bean
    public RestClient ollamaRestClient(OllamaProperties properties) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(properties.getRestTimeout());
        return RestClient
                .builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    @Bean
    public OllamaClient ollamaClient(@Qualifier("ollamaRestClient") RestClient client) {
        RestClientAdapter adapter = RestClientAdapter
                .create(client);

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();
        return factory.createClient(OllamaClient.class);
    }
}

