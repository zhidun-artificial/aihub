package ai.zhidun.app.hub.auth.controller;

import ai.zhidun.app.hub.auth.model.UserInfo;
import ai.zhidun.app.hub.common.Response;
import ai.zhidun.app.hub.auth.service.UserService;
import ai.zhidun.app.hub.auth.service.UserService.LoginResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证服务", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    public record LoginRequest(String username, String password) {

    }

    @PostMapping("/login")
    public Response<LoginResult> login(@RequestBody LoginRequest request) {
        return Response.ok(service.login(request.username, request.password));
    }

    public record RegisterRequest(String username, String password) {

    }

    @Tag(name = "test[for remove]")
    @PostMapping("/register")
    public Response<UserInfo> register(@RequestBody RegisterRequest request) {
        return Response.ok(service.register(request.username, request.password));
    }
}
