package ai.zhidun.app.hub.documents.service.impl;

import ai.zhidun.app.hub.common.BizError;
import ai.zhidun.app.hub.common.BizException;
import ai.zhidun.app.hub.documents.controller.DocumentController.SemanticSearchDocument;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.KnnQuery;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.mapping.DynamicMapping;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.MsearchRequest;
import co.elastic.clients.elasticsearch.core.MsearchResponse;
import co.elastic.clients.elasticsearch.core.msearch.MultiSearchItem;
import co.elastic.clients.elasticsearch.core.msearch.MultiSearchResponseItem;
import co.elastic.clients.elasticsearch.core.msearch.RequestItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import co.elastic.clients.json.JsonData;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzh.BgeSmallZhEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Slf4j
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
                "bge-small-zh", new BgeSmallZhEmbeddingModel(),
                "bge-small-en-v15-q", new BgeSmallEnV15QuantizedEmbeddingModel(),
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

        if (isIndexExists(indexName)) {
            throw new BizException(HttpStatus.BAD_REQUEST,
                    BizError.error("index already isIndexExistsisIndexExists"));
        }

        createIndex(id, modelId, indexName);

        return ElasticsearchEmbeddingStore.builder()
                .restClient(restClient)
                .indexName(indexName)
                .build();
    }

    private void createIndex(String baseId, String modelId, String indexName) {
        EmbeddingModel model = embeddingModel(modelId);

        TypeMapping mapping = TypeMapping.of(m -> m
                .dynamic(DynamicMapping.False)
                .properties("metadata", p -> p
                        .object(o -> o
                                .properties("fileName", f -> f
                                        .keyword(k -> k)
                                )
                                .properties("url", f -> f
                                        .keyword(k -> k)
                                )
                                .properties("documentId", f -> f
                                        .keyword(k -> k)
                                )
                                .properties("creator", f -> f
                                    .keyword(k -> k)
                                )
                                .properties("createTime", f -> f
                                    .date(k -> k)
                                )
                        )
                )
                .properties("text", p -> p
                        .text(t -> t)
                )
                .properties("vector", p -> p
                        .denseVector(d -> d
                                .dims(model.dimension())
                                .index(true)
                                //todo make it configurable
                                .similarity("l2_norm")
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

    private boolean isIndexExists(String indexName) {
        try {
            return client
                    .indices()
                    .exists(b -> b
                            .index(indexName))
                    .value();
        } catch (IOException e) {
            throw new BizException(HttpStatus.INTERNAL_SERVER_ERROR,
                    BizError.error("check index isIndexExistsisIndexExists failed"));
        }
    }

    public void delete(String baseId) {
        String indexName = indexName(baseId);
        if (isIndexExists(indexName)) {
            try {
                client
                        .indices()
                        .delete(b -> b
                                .index(indexName)
                                .allowNoIndices(true)
                        );
            } catch (IOException e) {
                throw new BizException(HttpStatus.INTERNAL_SERVER_ERROR,
                        BizError.error("delete index failed"));
            }
        } else {
            log.warn("index {} not isIndexExistsisIndexExists, skip delete!", indexName);
        }
    }

    /// 请确保es对应的索引存在，否则是未定义行为
    ///
    /// @param baseId baseId
    /// @return ElasticsearchEmbeddingStore
    public @NonNull ElasticsearchEmbeddingStore build(String baseId) {
        return ElasticsearchEmbeddingStore.builder()
                .restClient(restClient)
                .indexName(indexName(baseId))
                .build();
    }

    /// 触发es的刷新，建议放在批量操作之后
    public void refresh() {
        try {
            client.indices().refresh();
        } catch (IOException e) {
            log.warn("refresh failed", e);
        }
    }

    public boolean exists(String baseId) {
        return isIndexExists(indexName(baseId));
    }

    public record DocumentInfo(String documentId, Long createTime) {

    }

    public record Result(DocumentInfo metadata) {

    }

    public List<String> semanticSearch(SemanticSearchDocument request){

      MsearchRequest msearchRequest = MsearchRequest.of(b -> {
            Map<EmbeddingModel, Response<Embedding>> cacheEmbeddings = new HashMap<>(10);

            for (String baseId : request.baseIds()) {

                if (exists(baseId)) {
                  EmbeddingModel embeddingModel = model(baseId);

                  Response<Embedding> response = cacheEmbeddings.computeIfAbsent(embeddingModel,
                      model -> model.embed(request.query()));
                  Query query;
                  if (request.type() == 0) {

                    KnnQuery.Builder kq = new KnnQuery.Builder()
                        .field("vector")
                        .queryVector(response.content().vectorAsList());
                    query = Query.of(q -> q.knn(kq.build()));
                  } else {
                    query = Query.of(q -> q
                        .match(m -> m.field("text")
                            .query(request.query())));
                  }

                  RequestItem item = RequestItem.of(i -> i
                        .header(h -> h.index(indexName(baseId)))
                        .body(a -> a
                            .collapse(f -> f.field("metadata.documentId"))
                            .size(request.top())
                            .query(query)
                            .minScore(request.minScore())
                            .sort(s -> s
                                .field(f -> f
                                    .field("metadata.createTime")
                                    .order(SortOrder.Desc)
                                )
                            )
                            .source(s -> s
                                .filter(f -> f.includes("metadata.documentId", "metadata.createTime")))
                        )
                    );
                    b.searches(item);
                }
            }
            return b;
    });

      PriorityQueue<DocumentInfo> queue = new PriorityQueue<>(request.top(),
          Comparator.comparing(DocumentInfo::createTime));

      try {
          MsearchResponse<Result> response = client.msearch(msearchRequest, Result.class);

          for (MultiSearchResponseItem<Result> responseItem : response.responses()) {
              MultiSearchItem<Result> items = responseItem.result();

              for (Hit<Result> hit : items.hits().hits()) {
                Result source = hit.source();
                  if (source != null) {
                    DocumentInfo metadata = source.metadata();
                    if (queue.size() < request.top()) {
                      queue.add(metadata);
                    } else {
                      if (Objects.requireNonNull(queue.peek()).createTime() < metadata.createTime()) {
                        queue.poll();
                        queue.add(metadata);
                      }
                    }
                  }
              }
          }

          return queue
              .stream()
              .map(DocumentInfo::documentId)
              .toList();

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
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

    public @NonNull EmbeddingModel model(String baseId) {
        IndexMeta meta = indexMeta(baseId);
        return embeddingModel(meta.modelId());
    }

    public @NonNull EmbeddingStoreIngestor ingest(String baseId) {
        EmbeddingModel model = model(baseId);

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
