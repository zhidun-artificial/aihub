package ai.zhidun.app.hub.documents.service;

import ai.zhidun.app.hub.auth.service.AuthSupport;
import ai.zhidun.app.hub.documents.controller.DocumentController;
import ai.zhidun.app.hub.documents.controller.DocumentController.SearchDocument;
import ai.zhidun.app.hub.documents.controller.DocumentController.SemanticSearchDocument;
import ai.zhidun.app.hub.documents.model.DocumentVo;
import ai.zhidun.app.hub.store.utils.FileParser.ParsedResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

public interface DocumentService {
    int STATUS_PENDING = 0;
    int STATUS_INGESTING = 1;
    int STATUS_FINISHED = 2;
    int STATUS_ERROR = 3;

    void rename(String id, String name);

    void delete(String id);

    void batchDelete(List<String> ids);

    void triggerIngest();

    IPage<DocumentVo> search(SearchDocument request);

    IPage<DocumentVo> searchBlocked(SearchDocument request);

    void retryIngest();

    List<DocumentVo> semanticSearch(SemanticSearchDocument request);

    record ReplaceResult(DocumentVo replaced, Unknown unknown) {

    }

    ReplaceResult replace(String id, MultipartFile file);

    record Unknown(String fileName, String contentType) {

    }

    record SaveResult(List<DocumentVo> saved, List<Unknown> unknowns) {

    }

    default SaveResult save(MultipartFile[] files, @RequestParam String libraryId) {
        return save(files, libraryId, AuthSupport.userId());
    }

    SaveResult save(MultipartFile[] files, @RequestParam String libraryId, String userId);

    SaveResult save(Collection<ParsedResult> files, @RequestParam String libraryId, String userId);
}
