package ai.zhidun.app.hub.documents.service;

import ai.zhidun.app.hub.documents.controller.KnowledgeBaseController;
import ai.zhidun.app.hub.documents.model.KnowledgeBaseVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Optional;

public interface KnowledgeBaseService {

    record CreateKnowledgeBase(
            String name,
            String embedModel,
            String description,
            List<String> tags,
            @Schema(description = "1-私密 2-团队 3-公开")
            Integer permit,
            JsonNode ext
    ) {

        public CreateKnowledgeBase(String name,
                               String embedModel,
                               String description,
                               List<String> tags,
                               @Schema(description = "1-私密 2-团队 3-公开")
                               Integer permit,
                               JsonNode ext) {
            this.name = name;
            this.embedModel = embedModel;
            this.tags = tags;
            this.permit = permit;
            this.description = description;
            this.ext = ext != null ? ext : JsonNodeFactory.instance.objectNode();
        }

    }

    KnowledgeBaseVo create(CreateKnowledgeBase create);

    record UpdateKnowledgeBase(
            String id,
            String name,
            String embedModel,
            String description,
            List<String> tags,
            JsonNode ext
    ) {

    }

    KnowledgeBaseVo update(UpdateKnowledgeBase update);

    void delete(String id);

    IPage<KnowledgeBaseVo> search(KnowledgeBaseController.SearchKnowledgeBase search);

    Optional<KnowledgeBaseVo> getFistByName(String libraryName);
}
