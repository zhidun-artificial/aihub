package ai.zhidun.app.knowledge.documents.service;

import ai.zhidun.app.knowledge.documents.controller.LibraryController.SearchLibrary;
import ai.zhidun.app.knowledge.documents.model.LibraryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Optional;

public interface LibraryService {
    LibraryVo create(String name);

    LibraryVo update(Integer id, String name);

    void delete(Integer id);

    record LibraryVoWithCount(@JsonUnwrapped LibraryVo vo, Integer docCount) {

    }

    IPage<LibraryVoWithCount> search(SearchLibrary search);

    Optional<LibraryVo> getFistByName(String libraryName);
}
