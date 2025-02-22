package ai.zhidun.app.hub.documents.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("blocked_words")
public class BlockedWord {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String value;

    private Boolean enabled;

    private String creator;

    // 下面两个字段由mysql/mariadb自动管理
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createTime;
}
