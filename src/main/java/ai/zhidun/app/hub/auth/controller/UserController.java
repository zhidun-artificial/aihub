package ai.zhidun.app.hub.auth.controller;

import ai.zhidun.app.hub.auth.model.UserVo;
import ai.zhidun.app.hub.auth.service.UserService;
import ai.zhidun.app.hub.common.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Deprecated
@Tag(name = "用户服务", description = "用户相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public Response<UserVo> create(@RequestParam String name,@RequestParam int permit) {
        return Response.ok(service.create(name, permit));
    }
}
