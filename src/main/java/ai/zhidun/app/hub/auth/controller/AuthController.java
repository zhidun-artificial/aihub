package ai.zhidun.app.hub.auth.controller;

import ai.zhidun.app.hub.auth.service.AuthSupport;
import ai.zhidun.app.hub.common.Response;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Tag(name = "认证服务", description = "用户认证相关接口")
@Controller
public class AuthController {

    @ResponseBody
    @Schema(deprecated = true, description = "接口只用于测试")
    @GetMapping("/api/v1/auth/show_me")
    public Response<String> register() {
        return Response.ok(AuthSupport.userDetail().toString());
    }

    @Value("${cas.success.forward-url}")
    private String targetUrl;

    @GetMapping("/api/v1/auth/login")
    public String login() {
        return "redirect:" + targetUrl;
    }

    @ResponseBody
    @Schema(deprecated = true, description = "接口只用于测试")
    @GetMapping("/index")
    public String index() {
        return "hello world";
    }
}
