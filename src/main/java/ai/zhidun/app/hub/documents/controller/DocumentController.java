package ai.zhidun.app.hub.documents.controller;

import ai.zhidun.app.hub.common.Response;
import ai.zhidun.app.hub.common.Response.Empty;
import ai.zhidun.app.hub.common.Response.PageVo;
import ai.zhidun.app.hub.common.Sort;
import ai.zhidun.app.hub.documents.model.DocumentVo;
import ai.zhidun.app.hub.documents.service.DocumentService;
import ai.zhidun.app.hub.documents.service.DocumentService.ReplaceResult;
import ai.zhidun.app.hub.documents.service.DocumentService.SaveResult;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文档管理", description = "文档相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<SaveResult> upload(@RequestPart MultipartFile[] files, @RequestParam String baseId) {
        return Response.ok(service.save(files, baseId));
    }

    @PutMapping("/{id}/rename")
    public Response<Empty> rename(@PathVariable String id, @RequestParam("name") String name) {
        service.rename(id, name);
        return Response.ok();
    }

    @PutMapping(path = "/{id}/replace", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<ReplaceResult> replace(@PathVariable String id, @RequestPart MultipartFile file) {
        return Response.ok(service.replace(id, file));
    }

    @DeleteMapping("/{id}")
    public Response<Empty> delete(@PathVariable String id) {
        service.delete(id);
        return Response.ok();
    }

    public record BatchDeleteDocument(List<String> ids) {
    }

    @PostMapping("/batch_delete")
    public Response<Empty> delete(@RequestBody BatchDeleteDocument request) {
        service.batchDelete(request.ids());
        return Response.ok();
    }

    @PostMapping("/retry_ingest")
    public Response<Empty> retryIngest() {
        service.retryIngest();
        return Response.ok();
    }

    public record SearchDocument(
            @Schema(description = "按名字检索")
            String key,
            @Schema(description = "知识库id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            String baseId,
            @Schema(defaultValue = "1", description = "从1开始")
            Integer pageNo,
            @Schema(defaultValue = "20")
            Integer pageSize,
            Sort sort) {

        public SearchDocument(String key, String baseId, Integer pageNo, Integer pageSize, Sort sort) {
            this.key = key;
            this.baseId = baseId;
            this.pageNo = pageNo != null ? pageNo : 1;
            this.pageSize = pageSize != null ? pageSize : 20;
            this.sort = sort != null ? sort: Sort.CREATED_AT_DESC;
        }
    }

    @PostMapping("/search")
    public Response<PageVo<DocumentVo>> search(@RequestBody SearchDocument request) {
        return Response.page(service.search(request));
    }

    public record SemanticSearchDocument(
        @Schema(description = "检索query")
        String query,
        @Schema(description = "0-语义检索 1-分词检索")
        int type,
        @Schema(description = "知识库ids")
        List<String> baseIds,
        @Schema(defaultValue = "0.3", description = "最小相似度")
        Double minScore,
        @Schema(defaultValue = "100", description = "从1开始")
        Integer top) {

        public Integer top() {
            return top != null ? top : 100;
        }
    }

    @PostMapping("/semantic_search")
    public Response<List<DocumentVo>> search(@RequestBody SemanticSearchDocument request) {
        return Response.list(service.semanticSearch(request));
    }

    @PostMapping("/blocked_document/search")
    public Response<PageVo<DocumentVo>> searchBlocked(@RequestBody SearchDocument request) {
        return Response.page(service.searchBlocked(request));
    }
}
