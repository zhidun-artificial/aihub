package ai.zhidun.app.hub.documents.service;

import ai.zhidun.app.hub.auth.service.JwtSupport;
import ai.zhidun.app.hub.documents.controller.DocumentController.SearchDocument;
import ai.zhidun.app.hub.documents.model.DocumentVo;
import ai.zhidun.app.hub.store.utils.FileParser.ParsedResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

public interface DocumentService {

    void rename(String id, String name);

    void delete(String id);

    IPage<DocumentVo> search(SearchDocument request);

    IPage<DocumentVo> searchBlocked(SearchDocument request);

    record ReplaceResult(DocumentVo replaced, Unknown unknown) {

    }

    ReplaceResult replace(String id, MultipartFile file);

    record Unknown(String fileName, String contentType) {

    }

    record SaveResult(List<DocumentVo> saved, List<Unknown> unknowns) {

    }

    default SaveResult save(MultipartFile[] files, @RequestParam String libraryId) {
        return save(files, libraryId, JwtSupport.userId());
    }


    SaveResult save(MultipartFile[] files, @RequestParam String libraryId, String userId);

    SaveResult save(Collection<ParsedResult> files, @RequestParam String libraryId, String userId);
}
