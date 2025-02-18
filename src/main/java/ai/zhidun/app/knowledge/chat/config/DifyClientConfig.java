package ai.zhidun.app.knowledge.chat.config;

import ai.zhidun.app.knowledge.chat.client.DifyChatFlowClient;
import ai.zhidun.app.knowledge.chat.client.DifyConversationClient;
import ai.zhidun.app.knowledge.chat.client.DifyTranslateClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
@EnableConfigurationProperties({DifyProperties.class})
public class DifyClientConfig {

    public static WebClient buildClient(DifyProperties properties, String apiKey) {
        var httpClient = HttpClient.create(
                        ConnectionProvider
                                .builder("dify")
                                .maxConnections(properties.getMaxConnection())
                                .pendingAcquireTimeout(properties.getPendingAcquireTimeout())
                                .disposeTimeout(properties.getDisposeTimeout())
                                .build())
                .responseTimeout(properties.getResponseTimeout());

        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .baseUrl(properties.getBaseUrl())
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(properties.getMaxInMemorySize()))
                .build();
    }

    @Bean
    public WebClient chatClient(DifyProperties properties) {
        return buildClient(properties, properties.getTokens().chatAgent());
    }

    @Bean
    public RestClient chatRestClient(DifyProperties properties) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(properties.getRestTimeout());
        return RestClient
                .builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getTokens().chatAgent())
                .requestFactory(factory)
                .build();
    }

    @Bean
    public RestClient translateClient(DifyProperties properties) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(properties.getRestTimeout());
        return RestClient
                .builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getTokens().translate())
                .requestFactory(factory)
                .build();
    }

    @Bean
    public DifyChatFlowClient chatFlowClient(@Qualifier("chatClient") WebClient client) {
        WebClientAdapter adapter = WebClientAdapter
                .create(client);

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();
        return factory.createClient(DifyChatFlowClient.class);
    }

    @Bean
    public DifyTranslateClient difyTranslateClient(@Qualifier("translateClient") RestClient client) {
        RestClientAdapter adapter = RestClientAdapter
                .create(client);

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();
        return factory.createClient(DifyTranslateClient.class);
    }

    @Bean
    public DifyConversationClient difyConversationClient(@Qualifier("chatRestClient") RestClient client) {
        RestClientAdapter adapter = RestClientAdapter
                .create(client);

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();
        return factory.createClient(DifyConversationClient.class);
    }
}

