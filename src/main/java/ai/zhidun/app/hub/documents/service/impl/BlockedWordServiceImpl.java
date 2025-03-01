package ai.zhidun.app.hub.documents.service.impl;

import ai.zhidun.app.hub.auth.service.AuthSupport;
import ai.zhidun.app.hub.common.BizError;
import ai.zhidun.app.hub.common.BizException;
import ai.zhidun.app.hub.documents.controller.BlockedWordController;
import ai.zhidun.app.hub.documents.dao.BlockedWord;
import ai.zhidun.app.hub.documents.dao.BlockedWordMapper;
import ai.zhidun.app.hub.documents.model.BlockedWordVo;
import ai.zhidun.app.hub.documents.service.BlockedWordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BlockedWordServiceImpl extends ServiceImpl<BlockedWordMapper, BlockedWord> implements BlockedWordService {

    @CacheEvict(value = "block_rule", allEntries = true)
    @Override
    @Transactional
    public BlockedWordVo create(String blockedWord) {
        if (this.exists(Wrappers
                .lambdaQuery(BlockedWord.class)
                .eq(BlockedWord::getValue, blockedWord))) {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("敏感词已存在!"));
        }

        BlockedWord entity = new BlockedWord();
        entity.setValue(blockedWord);
        entity.setCreator(AuthSupport.userId());
        entity.setEnabled(true);
        this.save(entity);
        return BlockedWordVo.from(entity);
    }

    @CacheEvict(value = "block_rule", allEntries = true)
    @Override
    @Transactional
    public BlockedWordVo update(String id, String blockedWord) {
        if (this.getById(id) instanceof BlockedWord entity) {
            if (this.exists(Wrappers
                    .lambdaQuery(BlockedWord.class)
                    .eq(BlockedWord::getValue, blockedWord)
                    .ne(BlockedWord::getId, id))) {
                throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("敏感词已存在!"));
            }

            entity.setValue(blockedWord);
            this.updateById(entity);
            return BlockedWordVo.from(entity);
        } else {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("对应的id的敏感词不存在!"));
        }
    }

    @Override
    public IPage<BlockedWordVo> search(BlockedWordController.SearchBlockedWord request) {
        PageDTO<BlockedWord> page = new PageDTO<>(request.pageNo(), request.pageSize());

        LambdaQueryWrapper<BlockedWord> query = Wrappers
                .lambdaQuery(BlockedWord.class)
                .like(StringUtils.isNotBlank(request.key()), BlockedWord::getValue, "%" + request.key() + "%");

        return this
                .page(page, query)
                .convert(BlockedWordVo::from);
    }

    @Cacheable("block_rule")
    @Override
    public List<BlockedWordVo> all() {
        return this
                .list(Wrappers
                        .lambdaQuery(BlockedWord.class)
                        .eq(BlockedWord::getEnabled, true)
                )
                .stream()
                .map(BlockedWordVo::from)
                .toList();
    }

    @CacheEvict(value = "block_rule", allEntries = true)
    @Override
    public void delete(String id) {
        this.removeById(id);
    }

    @CacheEvict(value = "block_rule", allEntries = true)
    @Override
    public void enable(String id) {
        this.update(Wrappers.lambdaUpdate(BlockedWord.class)
                .eq(BlockedWord::getId, id)
                .set(BlockedWord::getEnabled, true));
    }

    @CacheEvict(value = "block_rule", allEntries = true)
    @Override
    public void disable(String id) {
        this.update(Wrappers.lambdaUpdate(BlockedWord.class)
                .eq(BlockedWord::getId, id)
                .set(BlockedWord::getEnabled, false));
    }
}
