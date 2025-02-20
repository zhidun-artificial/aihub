package ai.zhidun.app.hub.store.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("s3")
public record S3Properties(
        String accessKeyId,
        String secretAccessKey,
        String endpoint,
        String publicEndpoint
) {


}
