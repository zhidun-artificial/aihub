package ai.zhidun.app.hub.documents.controller;

import ai.zhidun.app.hub.common.Response;
import ai.zhidun.app.hub.common.Response.Empty;
import ai.zhidun.app.hub.common.Sort;
import ai.zhidun.app.hub.documents.model.KnowledgeBaseVo;
import ai.zhidun.app.hub.documents.service.KnowledgeBaseService;
import ai.zhidun.app.hub.documents.service.KnowledgeBaseService.CreateKnowledgeBase;
import ai.zhidun.app.hub.documents.service.KnowledgeBaseService.UpdateKnowledgeBase;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "知识库管理", description = "知识库相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/knowledge_base")
public class KnowledgeBaseController {

    private final KnowledgeBaseService service;

    public KnowledgeBaseController(KnowledgeBaseService service) {
        this.service = service;
    }

    @PostMapping
    public Response<KnowledgeBaseVo> create(@RequestBody CreateKnowledgeBase request) {
        return Response.ok(service.create(request));
    }

    @PutMapping
    public Response<KnowledgeBaseVo> update(@RequestBody UpdateKnowledgeBase request) {
        return Response.ok(service.update(request));
    }

    @DeleteMapping("/{id}")
    public Response<Empty> delete(@PathVariable String id) {
        service.delete(id);
        return Response.ok();
    }

    public record SearchKnowledgeBase(
            @Schema(description = "按名字检索")
            String key,
            @Schema(defaultValue = "1", description = "从1开始")
            Integer pageNo,
            @Schema(defaultValue = "20")
            Integer pageSize,
            Sort sort) {

        public SearchKnowledgeBase(String key, Integer pageNo, Integer pageSize, Sort sort) {
            this.key = key;
            this.pageNo = pageNo != null ? pageNo : 1;
            this.pageSize = pageSize != null ? pageSize : 20;
            this.sort = sort != null ? sort : Sort.CREATED_AT_DESC;
        }
    }

    @PostMapping("/search")
    public Response<Response.PageVo<KnowledgeBaseVo>> search(@RequestBody SearchKnowledgeBase request) {
        return Response.page(service.search(request));
    }


}
