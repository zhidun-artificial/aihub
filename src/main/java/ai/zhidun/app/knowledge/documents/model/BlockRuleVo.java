package ai.zhidun.app.knowledge.documents.model;

import ai.zhidun.app.knowledge.documents.dao.BlockRule;

public record BlockRuleVo(Integer id, String blockedWord) {
    public static BlockRuleVo from(BlockRule entity) {
        return new BlockRuleVo(entity.getId(), entity.getValue());
    }
}
