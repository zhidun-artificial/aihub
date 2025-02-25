package ai.zhidun.app.hub.chat.service.impl;

import ai.zhidun.app.hub.chat.controller.ChatController;
import ai.zhidun.app.hub.chat.dao.Message;
import ai.zhidun.app.hub.chat.dao.MessageMapper;
import ai.zhidun.app.hub.chat.model.QueryContext;
import ai.zhidun.app.hub.chat.model.MessageVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> {

    public IPage<MessageVo> messages(ChatController.SearchMessage request) {
        PageDTO<Message> page = new PageDTO<>(request.pageNo(), request.pageSize());

        LambdaQueryWrapper<Message> query = Wrappers
                .lambdaQuery(Message.class);

        query = request
                .sort()
                .sort(query, Message::getCreateTime, Message::getUpdateTime);

        IPage<Message> result = this.page(page, query);

        return result.convert(this::from);
    }

    private final JsonMapper mapper = new JsonMapper();

    @SneakyThrows
    private MessageVo from(Message message) {
        QueryContext ctx = mapper.reader().readValue(message.getContext(), QueryContext.class);
        return new MessageVo(
                message.getId(),
                message.getConversationId(),
                message.getQuery(),
                message.getAnswer(),
                ctx,
                message.getCreateTime().getTime(),
                message.getUpdateTime().getTime()
        );
    }

    @SneakyThrows
    public Message newMessage(String conversationId, String query, QueryContext ctx) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setQuery(query);
        message.setAnswer("");
        message.setContext(mapper.writeValueAsString(ctx));
        this.save(message);
        return message;
    }

    @SneakyThrows
    public void finishMessage(String conversationId, String answer) {
        this.update(Wrappers.lambdaUpdate(Message.class)
                .eq(Message::getConversationId, conversationId)
                .set(Message::getAnswer, answer));
    }
}
