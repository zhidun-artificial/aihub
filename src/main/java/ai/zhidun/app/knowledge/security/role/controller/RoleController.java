package ai.zhidun.app.knowledge.security.role.controller;

import ai.zhidun.app.knowledge.common.Response;
import ai.zhidun.app.knowledge.common.Response.Empty;
import ai.zhidun.app.knowledge.common.Response.PageVo;
import ai.zhidun.app.knowledge.security.role.model.RoleInfo;
import ai.zhidun.app.knowledge.security.role.model.RolePermissions;
import ai.zhidun.app.knowledge.security.role.service.RoleService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "角色服务", description = "角色管理相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    public record CreateRole(String name, String remarks) {

    }

    @PostMapping
    public Response<RoleInfo> create(@RequestBody CreateRole request) {
        RoleInfo info = service.create(request.name, request.remarks);
        return Response.ok(info);
    }

    public record UpdateRole(String name, String remarks) {

    }

    @PutMapping("/{id}")
    public Response<RoleInfo> create(@PathVariable Integer id, @RequestBody UpdateRole request) {
        RoleInfo info = service.update(id, request.name, request.remarks);
        return Response.ok(info);
    }

    public record SearchRoles(
            @Schema(description = "按名字检索")
            String key,
            @Schema(defaultValue = "1", description = "从1开始")
            Integer pageNo,
            @Schema(defaultValue = "20")
            Integer pageSize) {

        public SearchRoles(String key, Integer pageNo, Integer pageSize) {
            this.key = key;
            this.pageNo = pageNo != null ? pageNo : 1;
            this.pageSize = pageSize != null ? pageSize : 20;
        }
    }

    @PostMapping("/search")
    public Response<PageVo<RoleInfo>> search(@RequestBody SearchRoles request) {
        return Response.page(service.search(request));
    }

    @GetMapping("/{id}/permissions")
    public Response<RolePermissions> permission(@PathVariable Integer id) {
        return Response.ok(service.getPermissions(id));
    }

    @PutMapping("/{id}/permissions")
    public Response<RolePermissions> permission(@PathVariable Integer id, @RequestBody RolePermissions request) {
        service.putPermissions(id, request);
        return Response.ok(request);
    }

    @DeleteMapping("/{id}")
    public Response<Empty> delete(@PathVariable Integer id) {
        service.delete(id);
        return Response.ok();
    }
}
