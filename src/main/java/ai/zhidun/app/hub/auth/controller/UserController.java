package ai.zhidun.app.hub.auth.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户服务", description = "用户相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

}
