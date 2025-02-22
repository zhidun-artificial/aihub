package ai.zhidun.app.hub.documents.model;

public record DocumentVo(
        String id,
        String baseId,
        String title,
        String fileName,
        String url,
        String creator,
        String creatorName,
        String blockedReason,
        long createTime,
        long updateTime) {
}
