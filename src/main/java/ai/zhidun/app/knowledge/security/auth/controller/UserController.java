package ai.zhidun.app.knowledge.security.auth.controller;

import ai.zhidun.app.knowledge.common.Response;
import ai.zhidun.app.knowledge.common.Response.Empty;
import ai.zhidun.app.knowledge.common.Response.PageVo;
import ai.zhidun.app.knowledge.common.Sort;
import ai.zhidun.app.knowledge.security.auth.model.UserInfo;
import ai.zhidun.app.knowledge.security.auth.service.UserService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "用户服务", description = "用户相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    public record ImportUsers(@RequestPart("users") MultipartFile xlsx) {

    }

    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<Integer> importByXlsx(ImportUsers request) {
        return Response.ok(service.importByXlsx(request.xlsx));
    }

    public record SearchUsers(
            @Schema(description = "按名字检索")
            String key,
            @Schema(defaultValue = "1", description = "从1开始")
            Integer pageNo,
            @Schema(defaultValue = "20")
            Integer pageSize,
            Sort sort) {

        public SearchUsers(String key, Integer pageNo, Integer pageSize, Sort sort) {
            this.key = key;
            this.pageNo = pageNo != null ? pageNo : 1;
            this.pageSize = pageSize != null ? pageSize : 20;
            this.sort = sort != null ? sort: Sort.CREATED_AT_DESC;
        }
    }

    @PostMapping("/search")
    public Response<PageVo<UserInfo>> search(@RequestBody SearchUsers request) {
        return Response.page(service.search(request));
    }

    public record CreateUser(String username, String password) {

    }

    @PostMapping
    public Response<UserInfo> create(@RequestBody CreateUser request) {
        UserInfo info = service.register(request.username(), request.password());
        return Response.ok(info);
    }

    @DeleteMapping("/{id}")
    public Response<Empty> delete(@PathVariable Integer id) {
        service.delete(id);
        return Response.ok();
    }

    public record UpdateRoleId(Integer id, Integer role) {

    }

    @PutMapping("/update_role")
    public Response<Empty> updateRole(@RequestBody UpdateRoleId request) {
        service.updateRole(request.id(), request.role());
        return Response.ok();
    }

    public record UpdateSelfPassword(String oldPassword, String newPassword) {

    }

    @PutMapping("/update_self_password")
    public Response<Empty> updateSelfPassword(UpdateSelfPassword request) {
        service.updateSelfPassword(request.oldPassword(), request.newPassword());
        return Response.ok();
    }

    public record ResetPassword(Integer userId) {

    }

    @PutMapping("/reset_password")
    public Response<Empty> resetPassword(ResetPassword request) {
        service.resetPassword(request.userId());
        return Response.ok();
    }

}
