package ai.zhidun.app.hub.assistant.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("assistants")
public class Assistant {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("`name`")
    private String name;

    private String llmModel;

    private String systemPrompt;

    private Integer permit;

    private String ext;

    private String description;

    private String creator;

    // 下面两个字段由mysql/mariadb自动管理
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createTime;
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date updateTime;
}
