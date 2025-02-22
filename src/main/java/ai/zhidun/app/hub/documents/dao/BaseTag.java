package ai.zhidun.app.hub.documents.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("knowledge_base_tags")
public class BaseTag {

    @TableField("base_id")
    private String baseId;

    @TableField("tag")
    private String tag;
}