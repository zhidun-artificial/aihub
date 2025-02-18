package ai.zhidun.app.knowledge.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Iterator;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;

@Component
public class SseModelConverter implements ModelConverter {

  @Override
  public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
    if (type.getType() instanceof ArrayType arrayType) {
      JavaType contentType = arrayType.getContentType();
      if (contentType.isTypeOrSubTypeOf(ServerSentEvent.class)) {
        if (contentType.hasGenericTypes()) {
          if (contentType.getBindings() instanceof TypeBindings bindings &&
              bindings.getBoundType(0) instanceof JavaType realType) {
            // from Array<ServerSentEvent<T>> -> Array<T>
            type.setType(arrayType.withContentType(realType));
          }
        }
      }
    }

    return (chain.hasNext()) ? chain.next().resolve(type, context, chain) : null;
  }


  @Override
  public boolean isOpenapi31() {
    return true;
  }
}