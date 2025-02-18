package ai.zhidun.app.knowledge.documents.model;

public record DocumentVo(
        Integer id,
        Integer libraryId,
        String title,
        String fileName,
        String url,
        String rawUrl,
        String coverUrl,
        Integer creator,
        String creatorName,
        String blockedReason,
        long createTime) {
}
