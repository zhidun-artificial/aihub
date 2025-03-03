package ai.zhidun.app.hub.assistant.controller;

import ai.zhidun.app.hub.assistant.model.AssistantDetailVo;
import ai.zhidun.app.hub.assistant.model.AssistantVo;
import ai.zhidun.app.hub.assistant.service.AssistantService;
import ai.zhidun.app.hub.assistant.service.AssistantService.AssistantCreateParam;
import ai.zhidun.app.hub.assistant.service.AssistantService.AssistantUpdateParam;
import ai.zhidun.app.hub.common.Response;
import ai.zhidun.app.hub.common.Response.Empty;
import ai.zhidun.app.hub.common.Sort;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "ai助手管理", description = "ai助手相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/assistants")
public class AssistantController {

    private final AssistantService service;

    public AssistantController(AssistantService service) {
        this.service = service;
    }

    @PostMapping
    public Response<AssistantVo> create(@RequestBody AssistantCreateParam request) {
        return Response.ok(service.create(request));
    }

    @PutMapping
    public Response<AssistantVo> update(@RequestBody AssistantUpdateParam request) {
        return Response.ok(service.update(request));
    }

    @DeleteMapping("/{id}")
    public Response<Empty> delete(@PathVariable String id) {
        service.delete(id);
        return Response.ok();
    }

    public record SearchAssistant(
            @Schema(description = "按名字检索")
            String key,
            @Schema(defaultValue = "1", description = "从1开始")
            Integer pageNo,
            @Schema(defaultValue = "20")
            Integer pageSize,
            Sort sort,
            @Schema(description = "查询可编辑的知识库", defaultValue = "false")
            Boolean forEdit) {

        public Integer pageNo() {
            return pageNo != null ? pageNo : 1;
        }

        public Integer pageSize() {
            return pageSize != null ? pageSize : 20;
        }

        public Boolean forEdit() {
            return forEdit != null ? forEdit : false;
        }

    }

    @PostMapping("/search")
    public Response<Response.PageVo<AssistantVo>> search(@RequestBody SearchAssistant request) {
        return Response.page(service.search(request));
    }

    @GetMapping("/{id}/detail")
    public Response<AssistantDetailVo> search(@PathVariable String id) {
        return Response.ok(service.detail(id));
    }


}