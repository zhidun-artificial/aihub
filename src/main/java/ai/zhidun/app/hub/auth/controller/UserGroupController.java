package ai.zhidun.app.hub.auth.controller;

import ai.zhidun.app.hub.auth.model.UserGroupInfo;
import ai.zhidun.app.hub.auth.service.UserGroupService;
import ai.zhidun.app.hub.common.Response;
import ai.zhidun.app.hub.common.Response.Empty;
import ai.zhidun.app.hub.common.Response.PageVo;
import ai.zhidun.app.hub.common.Sort;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
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
            Sort sort) {

        public SearchUserGroups(String key, Integer pageNo, Integer pageSize, Sort sort) {
            this.key = key;
            this.pageNo = pageNo != null ? pageNo : 1;
            this.pageSize = pageSize != null ? pageSize : 20;
            this.sort = sort != null ? sort: Sort.CREATED_AT_DESC;
        }
    }

    @PostMapping("/search")
    public Response<PageVo<UserGroupInfo>> search(@RequestBody SearchUserGroups request) {
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

    public record CreateUserGroup(String name, String description, @JsonAnySetter JsonNode ext) {

    }

    @PostMapping
    public Response<UserGroupInfo> create(@RequestBody CreateUserGroup request) {
        UserGroupInfo info = service.insert(request.name(), request.description(), request.ext());
        return Response.ok(info);
    }

    public record UpdateUserGroup(String description, @JsonAnySetter JsonNode ext) {

    }

    @PutMapping("/{id}")
    public Response<UserGroupInfo> update(@PathVariable String id, @RequestBody UpdateUserGroup request) {
        UserGroupInfo info = service.update(id, request.description(), request.ext());
        return Response.ok(info);
    }

    @DeleteMapping("/{id}")
    public Response<Empty> delete(@PathVariable String id) {
        service.delete(id);
        return Response.ok();
    }
}
