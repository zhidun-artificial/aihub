package ai.zhidun.app.hub.auth.controller;

import ai.zhidun.app.hub.auth.service.AuthSupport;
import ai.zhidun.app.hub.common.Response;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证服务", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Schema(deprecated = true, description = "接口只用于测试")
    @GetMapping("/show_me")
    public Response<String> register() {
        return Response.ok(AuthSupport.userDetail().toString());
    }
}
