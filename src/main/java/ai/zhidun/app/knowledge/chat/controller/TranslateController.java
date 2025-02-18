package ai.zhidun.app.knowledge.chat.controller;

import ai.zhidun.app.knowledge.chat.service.TranslateService;
import ai.zhidun.app.knowledge.chat.service.TranslateService.TranslateRecord;
import ai.zhidun.app.knowledge.common.Response;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "翻译", description = "翻译相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/translate")
public class TranslateController {

    private final TranslateService service;

    public TranslateController(TranslateService service) {
        this.service = service;
    }

    public record Request(
            @Schema(description = "目标语言，默认中文", defaultValue = "Chinese")
            String targetLang,
            List<String> texts) {

    }

    @PostMapping
    public Response<List<TranslateRecord>> translate(@RequestBody Request request) {
        String targetLang = request.targetLang() != null ? request.targetLang : "Chinese";
        return Response.ok(service.translate(targetLang, request.texts));
    }
}
