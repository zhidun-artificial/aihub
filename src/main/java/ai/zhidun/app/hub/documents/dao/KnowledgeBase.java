package ai.zhidun.app.hub.documents.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("knowledge_base")
public class KnowledgeBase {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String name;

    private Integer permit;

    private String description;

    @TableField("embed_model")
    private String embedModel;

    private String ext;

    private String creator;

    private Boolean alive;

    // 下面两个字段由mysql/mariadb自动管理
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createTime;
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date updateTime;
}