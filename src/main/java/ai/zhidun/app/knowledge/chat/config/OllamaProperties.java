package ai.zhidun.app.knowledge.chat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;


@Data
@ConfigurationProperties("ollama")
public class OllamaProperties {

    private String baseUrl;
    private int maxConnection = 2000;
    private Duration pendingAcquireTimeout = Duration.ofSeconds(3);
    private Duration disposeTimeout = Duration.ofSeconds(3);
    private Duration responseTimeout = Duration.ofSeconds(120);

    private Duration restTimeout = Duration.ofSeconds(20);

    private int maxInMemorySize = 128 * 1024 * 1024;

    private String translateTemplate;

    private String translateSystem;

    private String translateModel;
}
