package ai.zhidun.app.hub.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    int insertIgnoreBatchSomeColumn4Mysql(List<Message> list);
}