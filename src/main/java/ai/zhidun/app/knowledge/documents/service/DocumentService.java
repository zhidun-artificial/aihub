package ai.zhidun.app.knowledge.documents.service;

import ai.zhidun.app.knowledge.documents.controller.DocumentController.SearchDocument;
import ai.zhidun.app.knowledge.documents.model.DocumentVo;
import ai.zhidun.app.knowledge.security.auth.service.JwtSupport;
import ai.zhidun.app.knowledge.store.utils.FileParser.ParsedResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

public interface DocumentService {

    void rename(Integer id, String name);

    void delete(Integer id);

    IPage<DocumentVo> search(SearchDocument request);

    IPage<DocumentVo> searchBlocked(SearchDocument request);

    record ReplaceResult(DocumentVo replaced, Unknown unknown) {

    }

    ReplaceResult replace(Integer id, MultipartFile file);

    record Unknown(String fileName, String contentType) {

    }

    record SaveResult(List<DocumentVo> saved, List<Unknown> unknowns) {

    }

    default SaveResult save(MultipartFile[] files, @RequestParam Integer libraryId) {
        return save(files, libraryId, JwtSupport.userId());
    }


    SaveResult save(MultipartFile[] files, @RequestParam Integer libraryId, Integer userId);

    SaveResult save(Collection<ParsedResult> files, @RequestParam Integer libraryId, Integer userId);
}
