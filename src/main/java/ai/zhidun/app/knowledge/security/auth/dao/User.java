package ai.zhidun.app.knowledge.security.auth.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String email;
    /**
     * 可以考虑只存储摘要而不是密码原始值
     */
    private String password;
    @TableField("register_time")
    private Date registerTime;
    @TableField("last_login_time")
    private Date lastLoginTime;
    @TableField("role_id")
    private Integer roleId;

    // 下面两个字段由mysql/mariadb自动管理
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createTime;
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date updateTime;
}