package ai.zhidun.app.knowledge.chat.service;

import ai.zhidun.app.knowledge.chat.controller.OpLogController.SearchOplogs;
import ai.zhidun.app.knowledge.chat.model.OpLogVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface OpLogService {

    OpLogVo newOpLog(String conversationId,String name, Integer creator);

    void addMessageCount(String conversationId);

    void rename(String conversationId, String newName);

    IPage<OpLogVo> search(SearchOplogs request);
}
