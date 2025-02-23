package ai.zhidun.app.hub.assistant.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("assistant_bases")
public class AssistantBaseMap {
    @TableField("`assistant_id`")
    private String assistantId;

    @TableField("`base_id`")
    private String baseId;
}
