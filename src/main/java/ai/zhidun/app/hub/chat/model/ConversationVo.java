package ai.zhidun.app.hub.chat.model;

public record ConversationVo(
        String id,
        String name,
        String creator,
        String creatorName,
        long createTime,
        long updateTime) {
}
