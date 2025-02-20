package ai.zhidun.app.hub.auth.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_groups")
public class UserGroup {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("admin_id")
    private String adminId;

    private String name;

    private Boolean alive;

    private String creator;

    private String description;

    // 留给前端的任意扩展字段，上层展开为JsonObject
    private String ext;

    // 下面两个字段由mysql/mariadb自动管理
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createTime;
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date updateTime;
}
