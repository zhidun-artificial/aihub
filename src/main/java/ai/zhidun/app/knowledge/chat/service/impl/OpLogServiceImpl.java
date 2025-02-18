package ai.zhidun.app.knowledge.chat.service.impl;

import ai.zhidun.app.knowledge.chat.controller.OpLogController.SearchOplogs;
import ai.zhidun.app.knowledge.chat.dao.OpLog;
import ai.zhidun.app.knowledge.chat.dao.OpLogMapper;
import ai.zhidun.app.knowledge.chat.model.OpLogVo;
import ai.zhidun.app.knowledge.chat.service.OpLogService;
import ai.zhidun.app.knowledge.security.auth.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OpLogServiceImpl extends ServiceImpl<OpLogMapper, OpLog> implements OpLogService {

    private final UserService userService;

    public OpLogServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OpLogVo newOpLog(String conversationId, String name, Integer creator) {
        OpLog entity = new OpLog();
        entity.setName(name);
        entity.setConversationId(conversationId);
        entity.setCreator(creator);
        entity.setCount(1);
        this.save(entity);

        entity = this.getById(entity.getId());
        return from(entity);
    }

    @Override
    public void addMessageCount(String conversationId) {
        this.lambdaUpdate()
                .eq(OpLog::getConversationId, conversationId)
                .setIncrBy(OpLog::getCount, 1)
                .update();
    }

    @Override
    public void rename(String conversationId, String newName) {
        this.lambdaUpdate()
                .eq(OpLog::getConversationId, conversationId)
                .set(OpLog::getName, newName)
                .update();
    }

    @Override
    public IPage<OpLogVo> search(SearchOplogs request) {
        PageDTO<OpLog> page = new PageDTO<>(request.pageNo(), request.pageSize());

        LambdaQueryWrapper<OpLog> query = Wrappers.lambdaQuery();
        query = query
                .and(StringUtils.isNotBlank(request.key()), c -> c.like(OpLog::getName, "%" + request.key() + "%"));

        switch (request.sort()) {
            case CREATED_AT_ASC,CREATED_AT_DESC -> {
                query.and(request.begin() != null, c -> c.gt(OpLog::getCreateTime, request.begin()));
                query.and(request.end() != null, c -> c.lt(OpLog::getCreateTime, request.end()));
            }
            case UPDATED_AT_ASC,UPDATED_AT_DESC -> {
                query.and(request.begin() != null, c -> c.gt(OpLog::getUpdateTime, request.begin()));
                query.and(request.end() != null, c -> c.lt(OpLog::getUpdateTime, request.end()));
            }
        }

        switch (request.sort()) {
            case CREATED_AT_DESC -> query.orderByDesc(OpLog::getCreateTime);
            case CREATED_AT_ASC -> query.orderByAsc(OpLog::getCreateTime);
            case UPDATED_AT_ASC -> query.orderByAsc(OpLog::getUpdateTime);
            case UPDATED_AT_DESC -> query.orderByDesc(OpLog::getUpdateTime);
        }

        return this
                .page(page, query)
                .convert(this::from);
    }

    public OpLogVo from(OpLog opLog) {
        return new OpLogVo(opLog.getId(),
                opLog.getConversationId(),
                opLog.getName(),
                opLog.getCreator(),
                userService.name(opLog.getCreator()),
                opLog.getCount(),
                opLog.getCreateTime().getTime(),
                opLog.getUpdateTime().getTime());
    }
}
