package ai.zhidun.app.hub.team.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("organ")
public class Organ {
	@TableId(type = IdType.ASSIGN_UUID)
    private String id;
	
	@TableField(value = "ORGAN_NAME")
	private String organName;
	
	@TableField(value = "ORGAN_FATHER")
	private String organFather;
	
	@TableField(value = "status")
	private String status;
}
