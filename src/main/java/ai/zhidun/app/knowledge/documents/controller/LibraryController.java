package ai.zhidun.app.knowledge.documents.controller;

import ai.zhidun.app.knowledge.common.Response;
import ai.zhidun.app.knowledge.common.Response.Empty;
import ai.zhidun.app.knowledge.common.Response.PageVo;
import ai.zhidun.app.knowledge.common.Sort;
import ai.zhidun.app.knowledge.documents.model.LibraryVo;
import ai.zhidun.app.knowledge.documents.service.LibraryService;
import ai.zhidun.app.knowledge.documents.service.LibraryService.LibraryVoWithCount;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "文库管理", description = "文库相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/libraries")
public class LibraryController {

    private final LibraryService service;

    public LibraryController(LibraryService service) {
        this.service = service;
    }

    public record CreateLibrary(String name) {

    }

    @PostMapping
    public Response<LibraryVo> create(@RequestBody CreateLibrary request) {
        return Response.ok(service.create(request.name()));
    }

    public record UpdateLibrary(Integer id, String name) {
    }

    @PutMapping
    public Response<LibraryVo> update(@RequestBody UpdateLibrary request) {
        return Response.ok(service.update(request.id(), request.name()));
    }

    @DeleteMapping("/{id}")
    public Response<Empty> delete(@PathVariable Integer id) {
        service.delete(id);
        return Response.ok();
    }

    public record SearchLibrary(
            @Schema(description = "按名字检索")
            String key,
            @Schema(defaultValue = "1", description = "从1开始")
            Integer pageNo,
            @Schema(defaultValue = "20")
            Integer pageSize,
            Sort sort) {

        public SearchLibrary(String key, Integer pageNo, Integer pageSize, Sort sort) {
            this.key = key;
            this.pageNo = pageNo != null ? pageNo : 1;
            this.pageSize = pageSize != null ? pageSize : 20;
            this.sort = sort != null ? sort: Sort.CREATED_AT_DESC;
        }
    }

    @PostMapping("/search")
    public Response<PageVo<LibraryVoWithCount>> search(@RequestBody SearchLibrary request) {
        return Response.page(service.search(request));
    }


}
