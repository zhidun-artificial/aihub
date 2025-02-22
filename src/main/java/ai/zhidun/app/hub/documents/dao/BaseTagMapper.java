package ai.zhidun.app.hub.documents.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BaseTagMapper extends BaseMapper<BaseTag> {

    int insertIgnoreBatchSomeColumn4Mysql(List<BaseTag> list);
}