package ai.zhidun.app.hub.chat.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("messages")
public class Message {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("`conversation_id`")
    private String conversationId;

    private String query;

    private String answer;

    private String context;

    // 下面两个字段由mysql/mariadb自动管理
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createTime;
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date updateTime;
}
