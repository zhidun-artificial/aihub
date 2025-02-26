package ai.zhidun.app.hub.auth.controller;

import ai.zhidun.app.hub.auth.model.UserVo;
import ai.zhidun.app.hub.auth.service.UserService;
import ai.zhidun.app.hub.common.Response;
import ai.zhidun.app.hub.common.Response.Empty;
import ai.zhidun.app.hub.common.Response.PageVo;
import ai.zhidun.app.hub.common.Sort;
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
            @Schema
            String groupId,
            @Schema(defaultValue = "1", description = "从1开始")
            Integer pageNo,
            @Schema(defaultValue = "20")
            Integer pageSize,
            Sort sort) {

        public SearchUsers(String key, String groupId, Integer pageNo, Integer pageSize, Sort sort) {
            this.key = key;
            this.groupId = groupId;
            this.pageNo = pageNo != null ? pageNo : 1;
            this.pageSize = pageSize != null ? pageSize : 20;
            this.sort = sort != null ? sort: Sort.CREATED_AT_DESC;
        }
    }

    @PostMapping("/search")
    public Response<PageVo<UserVo>> search(@RequestBody SearchUsers request) {
        return Response.page(service.search(request));
    }

    public record CreateUser(String username, String password) {

    }

    @PostMapping
    public Response<UserVo> create(@RequestBody CreateUser request) {
        UserVo info = service.register(request.username(), request.password());
        return Response.ok(info);
    }

    @DeleteMapping("/{id}")
    public Response<Empty> delete(@PathVariable String id) {
        service.delete(id);
        return Response.ok();
    }
}
