package ai.zhidun.app.hub.documents.service;

import ai.zhidun.app.hub.documents.controller.KnowledgeBaseController;
import ai.zhidun.app.hub.documents.model.KnowledgeBaseVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
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
            @JsonAnySetter JsonNode ext
    ) {

    }
    KnowledgeBaseVo create(CreateKnowledgeBase create);

    record UpdateKnowledgeBase(
            String id,
            String name,
            String embedModel,
            String description,
            List<String> tags,
            @JsonAnySetter JsonNode ext
    ) {

    }

    KnowledgeBaseVo update(UpdateKnowledgeBase update);

    void delete(String id);

    IPage<KnowledgeBaseVo> search(KnowledgeBaseController.SearchKnowledgeBase search);

    Optional<KnowledgeBaseVo> getFistByName(String libraryName);
}
