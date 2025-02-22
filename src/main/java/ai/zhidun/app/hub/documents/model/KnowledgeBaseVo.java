package ai.zhidun.app.hub.documents.model;

import com.fasterxml.jackson.databind.JsonNode;

public record KnowledgeBaseVo(
        String id,
        String name,
        String creator,
        String creatorName,
        Integer docCount,
        JsonNode ext,
        long createTime,
        long updateTime) {
}