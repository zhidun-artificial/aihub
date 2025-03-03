package ai.zhidun.app.hub.documents.service;

import ai.zhidun.app.hub.assistant.config.ModelProperties;
import ai.zhidun.app.hub.documents.service.impl.VectorStoreService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ModelService {

    private final VectorStoreService service;

    private final ModelProperties properties;

    public ModelService(VectorStoreService service, ModelProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    public record ModelsInfo (List<String> embeddings, List<String> llm) {

    }


    public ModelsInfo models() {
        List<String> embeddings = new ArrayList<>(service.embeddingModels().keySet());

        return new ModelsInfo(embeddings, properties.getOllamaLlm().getModels());
    }
}
