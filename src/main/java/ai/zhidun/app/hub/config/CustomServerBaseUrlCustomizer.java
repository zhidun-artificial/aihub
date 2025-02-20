package ai.zhidun.app.hub.config;

import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetSocketAddress;

@Component
public class CustomServerBaseUrlCustomizer implements ServerBaseUrlCustomizer {

  @Override
  public String customize(String serverBaseUrl, HttpRequest request) {
    try {
      if (request.getHeaders().getHost() instanceof InetSocketAddress address) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(serverBaseUrl)
            .host(address.getHostString());
        if (address.getPort() != 0) {
          builder.port(address.getPort());
        }
        serverBaseUrl = builder
            .build()
            .toUriString();
      }
    } catch (RuntimeException ex) {
      // nothing we can do
    }

    return serverBaseUrl;
  }
}