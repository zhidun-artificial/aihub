package ai.zhidun.app.knowledge.security.role.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("permissions")
public class Permission {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "parent_id")
    private Integer parentId;

    private String name;

    private String title;

    // 下面字段由mysql/mariadb自动管理
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createTime;

}
