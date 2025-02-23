package ai.zhidun.app.hub.assistant.model;

import ai.zhidun.app.hub.documents.service.KnowledgeBaseService.BaseInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

public record AssistantDetailVo(
        @JsonUnwrapped
        AssistantVo vo,
        List<BaseInfo> bases
) {
}
