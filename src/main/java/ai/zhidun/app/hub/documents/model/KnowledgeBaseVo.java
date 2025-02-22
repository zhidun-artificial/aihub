package ai.zhidun.app.hub.documents.model;

public record KnowledgeBaseVo(
        String id,
        String name,
        String creator,
        String creatorName,
        Integer docCount,
        long createTime,
        long updateTime) {
}