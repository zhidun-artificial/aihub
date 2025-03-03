package ai.zhidun.app.hub.team.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.zhidun.app.hub.common.Response;
import ai.zhidun.app.hub.team.dao.UsersTree;
import ai.zhidun.app.hub.team.service.UsersTreeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "用户组织架构", description = "用户组织架构查询接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/users_tree")
public class UsersTreeController {
	private final UsersTreeService usersTreeService;

    public UsersTreeController(UsersTreeService service) {
        this.usersTreeService = service;
    }
	
	@PostMapping("/get")
    public Response<List<UsersTree>> getUsersTree() {
		List<UsersTree> resulTrees = usersTreeService.getUsersTree();
        return Response.ok(resulTrees);
    }
}
