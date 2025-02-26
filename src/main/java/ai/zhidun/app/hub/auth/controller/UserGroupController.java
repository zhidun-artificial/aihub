package ai.zhidun.app.hub.auth.controller;

import ai.zhidun.app.hub.auth.model.UserGroupVo;
import ai.zhidun.app.hub.auth.service.UserGroupService;
import ai.zhidun.app.hub.auth.service.UserGroupService.CreateUserGroup;
import ai.zhidun.app.hub.auth.service.UserGroupService.UpdateUserGroup;
import ai.zhidun.app.hub.common.Response;
import ai.zhidun.app.hub.common.Response.Empty;
import ai.zhidun.app.hub.common.Response.PageVo;
import ai.zhidun.app.hub.common.Sort;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户组服务", description = "用户组相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/user_groups")
public class UserGroupController {
    private final UserGroupService service;

    public UserGroupController(UserGroupService service) {
        this.service = service;
    }


    public record SearchUserGroups(
            @Schema(description = "按名字检索")
            String key,
            @Schema(defaultValue = "1", description = "从1开始")
            Integer pageNo,
            @Schema(defaultValue = "20")
            Integer pageSize,
            boolean withUsers,
            @Schema(defaultValue = "CREATED_AT_DESC")
            Sort sort) {

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
    public Response<PageVo<UserGroupVo>> search(@RequestBody SearchUserGroups request) {
        return Response.page(service.search(request));
    }

    public record AddUser(String groupId, String userId) {

    }

    @PostMapping("/add_user")
    public Response<Empty> addUser(@RequestBody AddUser request) {
        service.addUser(request.groupId(), request.userId());
        return Response.ok();
    }

    @PostMapping("/delete_user")
    public Response<Empty> delUser(@RequestBody AddUser request) {
        service.deleteUser(request.groupId(), request.userId());
        return Response.ok();
    }

    @PostMapping
    public Response<UserGroupVo> create(@RequestBody CreateUserGroup request) {
        UserGroupVo info = service.insert(request);
        return Response.ok(info);
    }


    @PutMapping("/{id}")
    public Response<UserGroupVo> update(@PathVariable String id, @RequestBody UpdateUserGroup request) {
        UserGroupVo info = service.update(id, request);
        return Response.ok(info);
    }

    @DeleteMapping("/{id}")
    public Response<Empty> delete(@PathVariable String id) {
        service.delete(id);
        return Response.ok();
    }
}
