package ai.zhidun.app.hub.documents.service;

import ai.zhidun.app.hub.documents.controller.BlockedWordController;
import ai.zhidun.app.hub.documents.model.BlockedWordVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface BlockedWordService {
    BlockedWordVo create(String value);

    BlockedWordVo update(String id, String value);

    IPage<BlockedWordVo> search(BlockedWordController.SearchBlockedWord request);

    List<BlockedWordVo> all();

    void delete(String id);

    void enable(String id);

    void disable(String id);
}
