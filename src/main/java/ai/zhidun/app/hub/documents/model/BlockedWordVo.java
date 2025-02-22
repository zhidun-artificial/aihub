package ai.zhidun.app.hub.documents.model;

import ai.zhidun.app.hub.documents.dao.BlockedWord;

public record BlockedWordVo(
        String id,
        String value,
        Boolean enabled,
        long createTime) {
    public static BlockedWordVo from(BlockedWord entity) {
        return new BlockedWordVo(entity.getId(), entity.getValue(), entity.getEnabled(), entity.getCreateTime().getTime());
    }
}
