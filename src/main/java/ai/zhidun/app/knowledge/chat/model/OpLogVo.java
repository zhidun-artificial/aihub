package ai.zhidun.app.knowledge.chat.model;

public record OpLogVo (
        Integer id,
        String conversationId,
        String name,
        Integer creator,
        String creatorName,
        Integer count,
        long createTime,
        long updateTime) {
}
