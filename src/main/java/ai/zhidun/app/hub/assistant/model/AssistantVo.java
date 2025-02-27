package ai.zhidun.app.hub.assistant.model;

import com.fasterxml.jackson.databind.JsonNode;

public record AssistantVo(
        String id,
        String name,
        String llmModel,
        String systemPrompt,
        Integer permit,
        String groupId,
        String description,
        String creator,
        String creatorName,
        JsonNode ext,
        long createTime,
        long updateTime
) {
}
