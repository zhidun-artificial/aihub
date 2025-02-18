package ai.zhidun.app.knowledge.chat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;


@Data
@ConfigurationProperties("dify")
public class DifyProperties {

    public record Tokens(String translate, String chatAgent) {

    }

    private Tokens tokens;

    private String baseUrl;
    private int maxConnection = 2000;
    private Duration pendingAcquireTimeout = Duration.ofSeconds(3);
    private Duration disposeTimeout = Duration.ofSeconds(3);
    private Duration responseTimeout = Duration.ofSeconds(120);

    private Duration restTimeout = Duration.ofSeconds(20);

    private int maxInMemorySize = 128 * 1024 * 1024;

}
