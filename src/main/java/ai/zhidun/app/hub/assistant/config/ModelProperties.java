package ai.zhidun.app.hub.assistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@ConfigurationProperties(prefix = "model")
public class ModelProperties {

  @NestedConfigurationProperty
  private OllamaLlmProperties ollamaLlm;

}
