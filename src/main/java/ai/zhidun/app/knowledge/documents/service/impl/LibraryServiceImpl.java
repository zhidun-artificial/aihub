package ai.zhidun.app.knowledge.documents.service.impl;

import ai.zhidun.app.knowledge.documents.controller.LibraryController.SearchLibrary;
import ai.zhidun.app.knowledge.documents.dao.DocumentAggr;
import ai.zhidun.app.knowledge.documents.dao.DocumentAggrMapper;
import ai.zhidun.app.knowledge.documents.dao.Library;
import ai.zhidun.app.knowledge.documents.dao.LibraryMapper;
import ai.zhidun.app.knowledge.documents.model.LibraryVo;
import ai.zhidun.app.knowledge.documents.service.LibraryService;
import ai.zhidun.app.knowledge.security.auth.service.JwtSupport;
import ai.zhidun.app.knowledge.security.auth.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LibraryServiceImpl extends ServiceImpl<LibraryMapper, Library> implements LibraryService {

    private final DocumentAggrMapper documentAggrMapper;

    public final UserService userService;

    public LibraryServiceImpl(DocumentAggrMapper documentAggrMapper, UserService userService) {
        this.documentAggrMapper = documentAggrMapper;
        this.userService = userService;
    }


    public LibraryVo from(Library entity) {
        return new LibraryVo(entity.getId(), entity.getName(), entity.getCreator(), userService.name(entity.getCreator()), entity.getCreateTime().getTime(), entity.getUpdateTime().getTime());
    }

    @Override
    public LibraryVo create(String name) {
        Library entity = new Library();
        entity.setName(name);
        entity.setCreator(JwtSupport.userId());
        this.save(entity);

        entity = this.getById(entity.getId());

        return this.from(entity);
    }

    @Override
    public LibraryVo update(Integer id, String name) {
        Library entity = this.getById(id);
        entity.setName(name);
        entity.setId(id);
        this.updateById(entity);
        return this.from(entity);
    }

    @Override
    public void delete(Integer id) {
        this.removeById(id);
    }

    @Override
    public IPage<LibraryVoWithCount> search(SearchLibrary request) {
        PageDTO<Library> page = new PageDTO<>(request.pageNo(), request.pageSize());

        LambdaQueryWrapper<Library> query = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(request.key())) {
            query = query.like(Library::getName, "%" + request.key() + "%");
        }
        query = switch (request.sort()) {
            case UPDATED_AT_ASC -> query.orderByAsc(Library::getUpdateTime);
            case CREATED_AT_DESC -> query.orderByDesc(Library::getCreateTime);
            case UPDATED_AT_DESC -> query.orderByDesc(Library::getUpdateTime);
            case CREATED_AT_ASC -> query.orderByAsc(Library::getCreateTime);
        };

        IPage<LibraryVo> result = this
                .page(page, query)
                .convert(this::from);

        List<Integer> libraryIds = new ArrayList<>();
        for (LibraryVo vo : result.getRecords()) {
            libraryIds.add(vo.id());
        }

        Map<Integer, Integer> countMap = new HashMap<>();

        if (!libraryIds.isEmpty()) {
            LambdaQueryWrapper<DocumentAggr> wrapper = Wrappers
                    .lambdaQuery(DocumentAggr.class)
                    .select(DocumentAggr::getLibraryId, DocumentAggr::getCount)
                    .in(DocumentAggr::getLibraryId, libraryIds)
                    .groupBy(DocumentAggr::getLibraryId);

            for (DocumentAggr documentAggr : documentAggrMapper.selectList(wrapper)) {
                countMap.put(documentAggr.getLibraryId(), documentAggr.getCount());
            }
        }

        return result.convert(vo -> new LibraryVoWithCount(vo, countMap.getOrDefault(vo.id(), 0)));
    }

    @Override
    public Optional<LibraryVo> getFistByName(String libraryName) {

        LambdaQueryWrapper<Library> query = Wrappers.lambdaQuery(Library.class)
                .eq(Library::getName, libraryName);

        List<Library> list = this.list(query);
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(this.from(list.getFirst()));
        }
    }
}
