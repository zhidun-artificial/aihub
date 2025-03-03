package ai.zhidun.app.hub.auth.controller;

import ai.zhidun.app.hub.auth.service.TokenService;
import ai.zhidun.app.hub.auth.service.TokenService.TokenResult;
import ai.zhidun.app.hub.common.Response;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证服务", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/login_url")
    public Response<String> loginUrl() {
        return Response.ok(tokenService.loginUrl());
    }

    @GetMapping("/token")
    public Response<TokenResult> token(@RequestParam String ticket, @RequestParam(required = false) String service) {
        return Response.ok(tokenService.token(ticket, service));
    }
}
