package ai.zhidun.app.knowledge.documents.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("block_rules")
public class BlockRule {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String type;
    private String value;
    private Integer creator;

    // 下面两个字段由mysql/mariadb自动管理
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createTime;
}
