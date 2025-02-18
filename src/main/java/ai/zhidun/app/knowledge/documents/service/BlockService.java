package ai.zhidun.app.knowledge.documents.service;

import ai.zhidun.app.knowledge.documents.controller.BlockController;
import ai.zhidun.app.knowledge.documents.model.BlockRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface BlockService {
    BlockRuleVo create(String blockedWord);

    BlockRuleVo update(String id, String blockedWord);

    IPage<BlockRuleVo> search(BlockController.SearchBlock request);

    List<BlockRuleVo> all();

    void delete(Integer id);
}
