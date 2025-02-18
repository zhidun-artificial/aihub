package ai.zhidun.app.knowledge.security.role.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("roles")
public class Role {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String remarks;

    /**
     * 创建者的userId
     */
    private Integer creator;

    // 下面两个字段由mysql/mariadb自动管理
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createTime;
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date updateTime;
}