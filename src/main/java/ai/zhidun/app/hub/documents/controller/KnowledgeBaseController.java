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

import java.util.List;

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

  @GetMapping("/tags")
  public Response<List<String>> tags() {
    return Response.ok(service.tags());
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
      @Schema(description = "默认排序创建时间倒序", defaultValue = "CREATED_AT_DESC")
      Sort sort,
      @Schema(description = "查询可编辑的知识库", defaultValue = "false")
      boolean forEdit) {

    public Integer pageNo() {
      return pageNo != null ? pageNo : 1;
    }

    public Integer pageSize() {
      return pageSize != null ? pageSize : 20;
    }

    public Sort sort() {
      return sort != null ? sort : Sort.CREATED_AT_DESC;
    }
  }

  @PostMapping("/search")
  public Response<Response.PageVo<KnowledgeBaseVo>> search(@RequestBody SearchKnowledgeBase request) {
    return Response.page(service.search(request));
  }


}
