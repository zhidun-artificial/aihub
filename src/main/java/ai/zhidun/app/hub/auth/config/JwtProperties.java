package ai.zhidun.app.hub.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.time.Duration;

@ConfigurationProperties("jwt")
public record JwtProperties(
        Path jwtKey,
        String keyId,
        String issuer,
        String audience,
        Duration expiration,
        Duration refreshInterval,
        String head) {
}
