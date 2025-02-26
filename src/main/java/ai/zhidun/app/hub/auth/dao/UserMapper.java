package ai.zhidun.app.hub.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    List<UserInfo> selectByGroupIds(@Param("groupIds") List<String> groupIds);

    List<UserInfo> selectAdminByGroupIds(@Param("groupIds") List<String> groupIds);
}