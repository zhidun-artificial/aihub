package ai.zhidun.app.hub.documents.config;

import javax.net.ssl.SSLContext;
import lombok.SneakyThrows;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsConfig implements RestClientBuilderCustomizer {

  @Override
  public void customize(RestClientBuilder builder) {

  }

  @Override
  @SneakyThrows
  public void customize(HttpAsyncClientBuilder builder) {
    SSLContext sslContext;

    // 设置信任所有域
    SSLContextBuilder contextBuilder = SSLContexts.custom()
        .loadTrustMaterial(TrustAllStrategy.INSTANCE);
    sslContext = contextBuilder.build();

    builder
        .setSSLContext(sslContext)
        // 设置不做hostname过滤
        .setSSLHostnameVerifier(
            (host, session) -> host.equalsIgnoreCase(session.getPeerHost()));
  }
}
