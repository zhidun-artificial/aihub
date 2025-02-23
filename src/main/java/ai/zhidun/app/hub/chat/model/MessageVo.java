package ai.zhidun.app.hub.chat.model;

public record MessageVo(
        String id,
        String conversationId,
        String query,
        String answer,
        QueryContext files,
        long createTime,
        long updateTime) {
}
