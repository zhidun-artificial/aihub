package ai.zhidun.app.hub.documents.service;

import ai.zhidun.app.hub.documents.controller.KnowledgeBaseController;
import ai.zhidun.app.hub.documents.model.KnowledgeBaseVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Optional;

public interface KnowledgeBaseService {
    List<String> tags();

    record CreateKnowledgeBase(
            String name,
            String embedModel,
            String description,
            List<String> tags,
            @Schema(description = "0-公开 1-个人 2-团队")
            Integer permit,
            @Schema(description = "团队ID，当permit=2时必填")
            String groupId,
            JsonNode ext
    ) {

        public JsonNode ext() {
            return ext != null ? ext : JsonNodeFactory.instance.objectNode();
        }
    }

    KnowledgeBaseVo create(CreateKnowledgeBase create);

    record UpdateKnowledgeBase(
            String id,
            String name,
            String description,
            List<String> tags,
            JsonNode ext
    ) {

    }

    KnowledgeBaseVo update(UpdateKnowledgeBase update);

    void delete(String id);

    IPage<KnowledgeBaseVo> search(KnowledgeBaseController.SearchKnowledgeBase search);

    Optional<KnowledgeBaseVo> getFistByName(String libraryName);

    EmbeddingStore<TextSegment> embeddingStore(String id);


    record BaseInfo(String id, String name) {
    }

    List<BaseInfo> listBaseInfo(List<String> ids);
}
