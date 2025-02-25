package ai.zhidun.app.hub.documents.service.impl;

import ai.zhidun.app.hub.common.BizError;
import ai.zhidun.app.hub.common.BizException;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DynamicMapping;
import co.elastic.clients.elasticsearch._types.mapping.DynamicTemplate;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import co.elastic.clients.json.JsonData;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class VectorStoreService {

  public static final String BASE_ID = "baseId";
  public static final String MODEL_ID = "modelId";
  public static final String DIMENSION = "dimension";
  private final RestClient restClient;

  private final Map<String, EmbeddingModel> embeddingModels;

  private final ElasticsearchClient client;

  public VectorStoreService(RestClientBuilder builder, ElasticsearchClient client) {
    this.client = client;
    restClient = builder.build();

    // todo make it configurable
    embeddingModels = Map.of(
        "default", new BgeSmallEnV15QuantizedEmbeddingModel()
    );
  }

  public @NonNull Map<String, EmbeddingModel> embeddingModels() {
    return embeddingModels;
  }

  public @NonNull EmbeddingModel embeddingModel(String modelId) {
    if (embeddingModels.get(modelId) instanceof EmbeddingModel model) {
      return model;
    } else {
      throw new BizException(HttpStatus.INTERNAL_SERVER_ERROR, BizError.error("model not found"));
    }
  }

  private static String indexName(String baseId) {
    return "ai-hub-store-" + baseId;
  }

  public @NonNull ElasticsearchEmbeddingStore create(String id, String modelId) {
    String indexName = indexName(id);

    if (exists(indexName)) {
      throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("index already exists"));
    }

    createIndex(id, modelId, indexName);

    return ElasticsearchEmbeddingStore.builder()
        .restClient(restClient)
        .indexName(indexName)
        .build();
  }

  private void createIndex(String baseId, String modelId, String indexName) {
    DynamicTemplate metadata = DynamicTemplate.of(d -> d
        .pathMatch("metadata.*")
        .mapping(m -> m
            .keyword(k -> k)
        )
    );

    EmbeddingModel model = embeddingModel(modelId);

    TypeMapping mapping = TypeMapping.of(m -> m
        .dynamic(DynamicMapping.False)
        .dynamicTemplates(List.of(Map.of("metadata", metadata)))
        .properties("metadata", p -> p
            .object(o -> o
                .dynamic(DynamicMapping.Runtime)
            )
        )
        .properties("text", p -> p
            .text(t -> t)
        )
        .properties("vector", p -> p
            .denseVector(d -> d
                .dims(model.dimension())
            )
        )
        .meta(BASE_ID, JsonData.of(baseId))
        .meta(MODEL_ID, JsonData.of(modelId))
        .meta(DIMENSION, JsonData.of(model.dimension()))
    );

    try {
      client
          .indices()
          .create(builder -> builder
              .index(indexName)
              .settings(s -> s
                  //todo make it configurable
                  .numberOfReplicas("0")
                  .numberOfShards("1")
              )
              .mappings(mapping)
          );
    } catch (IOException e) {
      throw new BizException(HttpStatus.INTERNAL_SERVER_ERROR,
          BizError.error("create index failed"));
    }
  }

  public boolean exists(String indexName) {
    try {
      return client
          .indices()
          .exists(b -> b
              .index(indexName))
          .value();
    } catch (IOException e) {
      throw new BizException(HttpStatus.INTERNAL_SERVER_ERROR,
          BizError.error("check index exists failed"));
    }
  }

  public void delete(String baseId) {
    try {
      client
          .indices()
          .delete(b -> b.index(indexName(baseId)));
    } catch (IOException e) {
      throw new BizException(HttpStatus.INTERNAL_SERVER_ERROR,
          BizError.error("delete index failed"));
    }
  }

  /// 请确保es对应的索引存在，否则是未定义行为
  /// @param baseId baseId
  /// @return ElasticsearchEmbeddingStore
  public @NonNull ElasticsearchEmbeddingStore build(String baseId) {
    return ElasticsearchEmbeddingStore.builder()
        .restClient(restClient)
        .indexName(indexName(baseId))
        .build();
  }

  public record IndexMeta(String baseId, String modelId, int dimension) {

  }

  private IndexMeta indexMeta(String baseId) {
    try {
      String indexName = indexName(baseId);

      GetMappingResponse response = client
          .indices()
          .getMapping(r -> r
              .index(indexName)
          );

      Map<String, JsonData> meta = Objects.requireNonNull(response
              .get(indexName))
          .mappings()
          .meta();
      String modelId = meta.get(MODEL_ID).to(String.class);
      Integer dimension = meta.get(DIMENSION).to(Integer.class);
      return new IndexMeta(baseId, modelId, dimension);
    } catch (IOException e) {
      throw new BizException(HttpStatus.INTERNAL_SERVER_ERROR, BizError.error("get"));
    }

  }

  public @NonNull EmbeddingStoreIngestor ingest(String baseId) {
    IndexMeta meta = indexMeta(baseId);

    EmbeddingModel model = embeddingModel(meta.modelId());

    ElasticsearchEmbeddingStore store = build(baseId);

    return EmbeddingStoreIngestor
        .builder()
        .embeddingStore(store)
        .embeddingModel(model)
// todo add document splitter here
//        .documentSplitter()
        .build();
  }
}
