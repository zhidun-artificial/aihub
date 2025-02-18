package ai.zhidun.app.knowledge.documents.service.impl;

import ai.zhidun.app.knowledge.documents.controller.BlockController.SearchBlock;
import ai.zhidun.app.knowledge.documents.dao.BlockRule;
import ai.zhidun.app.knowledge.documents.dao.BlockRuleMapper;
import ai.zhidun.app.knowledge.documents.model.BlockRuleVo;
import ai.zhidun.app.knowledge.documents.service.BlockService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlockServiceImpl extends ServiceImpl<BlockRuleMapper, BlockRule> implements BlockService {

    @CacheEvict(value = "block_rule", allEntries = true)
    @Override
    public BlockRuleVo create(String blockedWord) {
        BlockRule entity = new BlockRule();
        entity.setType("blocked_word");
        entity.setValue(blockedWord);
        this.save(entity);
        return BlockRuleVo.from(entity);
    }

    @CacheEvict(value = "block_rule", allEntries = true)
    @Override
    public BlockRuleVo update(String id, String blockedWord) {
        BlockRule entity = new BlockRule();
        entity.setId(Integer.valueOf(id));
        entity.setType("blocked_word");
        entity.setValue(blockedWord);
        this.updateById(entity);
        return BlockRuleVo.from(entity);
    }

    @Override
    public IPage<BlockRuleVo> search(SearchBlock request) {
        PageDTO<BlockRule> page = new PageDTO<>(request.pageNo(), request.pageSize());

        LambdaQueryWrapper<BlockRule> query = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(request.key())) {
            query = query.like(BlockRule::getValue, "%" + request.key() + "%");
        }

        return this
                .page(page, query)
                .convert(BlockRuleVo::from);
    }

    @Cacheable("block_rule")
    @Override
    public List<BlockRuleVo> all() {
        return this
                .list()
                .stream()
                .map(BlockRuleVo::from)
                .toList();
    }

    @CacheEvict(value = "block_rule", allEntries = true)
    @Override
    public void delete(Integer id) {
        this.removeById(id);
    }
}
