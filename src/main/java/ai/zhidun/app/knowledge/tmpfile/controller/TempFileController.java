package ai.zhidun.app.knowledge.tmpfile.controller;

import ai.zhidun.app.knowledge.common.Response;
import ai.zhidun.app.knowledge.tmpfile.service.TmpFileService;
import ai.zhidun.app.knowledge.tmpfile.service.UploadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "临时文件管理", description = "临时文件相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/tmpfile")
public class TempFileController {

    private final TmpFileService service;

    public TempFileController(TmpFileService service) {
        this.service = service;
    }

    @Operation(description = "临时文件上传接口")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<List<UploadResult>> upload(@RequestPart("files") MultipartFile[] file) {
        return Response.ok(service.upload(file));
    }
}
