package ai.zhidun.app.knowledge.documents.controller;

import ai.zhidun.app.knowledge.common.Response;
import ai.zhidun.app.knowledge.common.Response.Empty;
import ai.zhidun.app.knowledge.common.Response.PageVo;
import ai.zhidun.app.knowledge.common.Sort;
import ai.zhidun.app.knowledge.documents.model.DocumentVo;
import ai.zhidun.app.knowledge.documents.service.DocImportService;
import ai.zhidun.app.knowledge.documents.service.DocumentService;
import ai.zhidun.app.knowledge.documents.service.DocumentService.ReplaceResult;
import ai.zhidun.app.knowledge.documents.service.DocumentService.SaveResult;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文档管理", description = "文档相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService service;

    private final DocImportService importService;

    public DocumentController(DocumentService service, DocImportService importService) {
        this.service = service;
        this.importService = importService;
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<SaveResult> upload(@RequestPart MultipartFile[] files, @RequestParam Integer libraryId) {
        return Response.ok(service.save(files, libraryId));
    }

    @PutMapping("/{id}/rename")
    public Response<Empty> rename(@PathVariable Integer id, @RequestParam("name") String name) {
        service.rename(id, name);
        return Response.ok();
    }

    @PutMapping(path = "/{id}/replace", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<ReplaceResult> replace(@PathVariable Integer id, @RequestPart MultipartFile file) {
        return Response.ok(service.replace(id, file));
    }

    @DeleteMapping("/{id}")
    public Response<Empty> delete(@PathVariable Integer id) {
        service.delete(id);
        return Response.ok();
    }

    public record SearchDocument(
            @Schema(description = "按名字检索")
            String key,
            @Schema(description = "文库id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            Integer libraryId,
            @Schema(defaultValue = "1", description = "从1开始")
            Integer pageNo,
            @Schema(defaultValue = "20")
            Integer pageSize,
            Sort sort) {

        public SearchDocument(String key, Integer libraryId, Integer pageNo, Integer pageSize, Sort sort) {
            this.key = key;
            this.libraryId = libraryId;
            this.pageNo = pageNo != null ? pageNo : 1;
            this.pageSize = pageSize != null ? pageSize : 20;
            this.sort = sort != null ? sort: Sort.CREATED_AT_DESC;
        }
    }

    @PostMapping("/search")
    public Response<PageVo<DocumentVo>> search(@RequestBody SearchDocument request) {
        return Response.page(service.search(request));
    }

    @PostMapping("/blocked_document/search")
    public Response<PageVo<DocumentVo>> searchBlocked(@RequestBody SearchDocument request) {
        return Response.page(service.searchBlocked(request));
    }

    public record ImportRequest(
            @Schema(defaultValue = "10")
            Integer parallelism
    ) {

    }

    @PostMapping("/import")
    public Response<Empty> importDocuments(@RequestBody ImportRequest request) {
        importService.importFromLocal(request);
        return Response.ok();
    }
}
