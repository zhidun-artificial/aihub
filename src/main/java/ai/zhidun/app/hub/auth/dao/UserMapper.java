package ai.zhidun.app.hub.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {


    int insertIgnoreBatchSomeColumn4Mysql(List<User> list);
}