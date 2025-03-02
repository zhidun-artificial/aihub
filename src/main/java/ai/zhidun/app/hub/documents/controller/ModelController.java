package ai.zhidun.app.hub.documents.controller;

import ai.zhidun.app.hub.common.Response;
import ai.zhidun.app.hub.documents.service.ModelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "模型信息", description = "模型信息接口")
@RestController
@RequestMapping("/api/v1/models")
public class ModelController {

    private final ModelService service;

    public ModelController(ModelService service) {
        this.service = service;
    }

    @GetMapping("/show")
    public Response<ModelService.ModelsInfo> models() {
        return Response.ok(service.models());
    }

}
