package ai.zhidun.app.hub.assistant.config;

import java.time.Duration;
import java.util.List;
import lombok.Data;

@Data
public class OllamaLlmProperties {

  private String baseUrl;

  /// default model
  private String defaultModel;

  private List<String> models;

  private Boolean logRequests;

  private Boolean logResponses;

  Double temperature;
  Integer topK;
  Double topP;
  Double repeatPenalty;
  Integer seed;
  Integer numPredict;
  List<String> stop;
  String format;
  Duration timeout;
}
