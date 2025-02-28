package ai.zhidun.app.hub.team.dao;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("user_trees_info")
public class UsersTree {
	@TableId(type = IdType.ASSIGN_UUID)
    private String id;
	
	private String organName;
	
	private String status;
	
	private String organFather;
	
	private JSONArray personArray = new JSONArray();
	
}
