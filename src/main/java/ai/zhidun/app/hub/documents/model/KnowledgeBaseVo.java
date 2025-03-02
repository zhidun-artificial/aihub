package ai.zhidun.app.hub.documents.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public record KnowledgeBaseVo(
        String id,
        String name,
        String embedModel,
        String creator,
        String creatorName,
        Integer docCount,
        JsonNode ext,
        List<String> tags,
        long createTime,
        long updateTime,
        Integer permit,
        String groupId) {
}