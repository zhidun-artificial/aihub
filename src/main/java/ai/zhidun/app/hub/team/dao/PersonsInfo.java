package ai.zhidun.app.hub.team.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("manex")
public class PersonsInfo {
	@TableId(type = IdType.ASSIGN_UUID)
	private String id;
	
	@TableField(value = "NAME")
	private String name;
	
	@TableField(value = "LOGINCODE")
	private String logincode;
	
//	@TableField(value = "NATION")
//	private String nation;
	
	@TableField(value = "ORGAN")
	private String organId;
	
	@TableField(value = "OFFICER")
	private String officer;
	
	@TableField(value = "IF_LEADER")
	private Integer ifLeader;


}
