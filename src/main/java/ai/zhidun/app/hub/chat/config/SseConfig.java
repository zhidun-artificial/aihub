package ai.zhidun.app.hub.chat.config;

import ai.zhidun.app.hub.chat.service.ChatEvent;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SseConfig {

  @Bean
  public OpenApiCustomizer customizer() {
    return openApi -> {
      ModelConverters instance = ModelConverters
          .getInstance();
      ResolvedSchema resolvedSchema = instance
          .readAllAsResolvedSchema(ChatEvent.class);

      ObjectSchema schema = new ObjectSchema();
      resolvedSchema
          .referencedSchemas
          .forEach((key, value) -> {
            openApi.schema(key, value);
            if (!value.equals(resolvedSchema.schema)) {
              if (value.getAllOf() instanceof List<?> list && list.contains(resolvedSchema.schema)) {
                schema.addOneOfItem(new Schema<>().$ref(key));
              }
            }
          });

      openApi.schema("_ChatEvent_", schema);
    };
  }
}
