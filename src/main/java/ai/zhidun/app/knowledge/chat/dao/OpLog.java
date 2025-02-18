package ai.zhidun.app.knowledge.chat.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("oplogs")
public class OpLog {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("`conversation_id`")
    private String conversationId;

    @TableField("`name`")
    private String name;

    private Integer creator;

    @TableField("`message_count`")
    private Integer count;

    // 下面两个字段由mysql/mariadb自动管理
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createTime;
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date updateTime;
}
