package ai.zhidun.app.hub.chat.controller;

import ai.zhidun.app.hub.documents.service.impl.VectorStoreService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

  private final VectorStoreService service;

  public TestController(VectorStoreService service) {
    this.service = service;
  }

  @PostMapping("/store")
  public String build(@RequestParam String id, @RequestParam(required = false) String model) {
    ElasticsearchEmbeddingStore store;
    if (!service.exists(id)) {
      store = service.create(id, model != null ? model : "default");
    } else {
      store = service.build(id);
    }

    Document document = FileSystemDocumentLoader.loadDocument("paper.pdf");
    document.metadata().put("id", "1");
    List<Document> documents = List.of(document);

    EmbeddingStoreIngestor.ingest(documents, store);

    return "ok";
  }

}