package ai.zhidun.app.hub.assistant.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AssistantBaseMapMapper extends BaseMapper<AssistantBaseMap> {

    int insertIgnoreBatchSomeColumn4Mysql(List<AssistantBaseMap> list);
}