package ai.zhidun.app.hub.documents.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("documents")
public class Document {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("`key`")
    private String key;

//    @TableField("`raw_key`")
//    private String rawKey;

//    @TableField("`cover_key`")
//    private String coverKey;

    private String bucket;

    private String title;

    @TableField("blocked_reason")
    private String blockedReason;

    @TableField("file_name")
    private String fileName;

    @TableField("base_id")
    private String baseId;

    private String creator;

    private Integer status;

    private Boolean alive;

    // 下面两个字段由mysql/mariadb自动管理
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createTime;
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date updateTime;
}
