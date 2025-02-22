package ai.zhidun.app.hub.documents.controller;

import ai.zhidun.app.hub.common.Response;
import ai.zhidun.app.hub.common.Response.Empty;
import ai.zhidun.app.hub.documents.model.BlockedWordVo;
import ai.zhidun.app.hub.documents.service.BlockedWordService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "屏蔽管理", description = "屏蔽相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/blocked_words")
public class BlockedWordController {

    private final BlockedWordService service;

    public BlockedWordController(BlockedWordService service) {
        this.service = service;
    }

    public record CreateBlockedWord(String value) {

    }

    @PostMapping
    public Response<BlockedWordVo> create(@RequestBody CreateBlockedWord request) {
        return Response.ok(service.create(request.value()));
    }

    public record UpdateBlockedWord(String id, String value) {

    }

    @PutMapping
    public Response<BlockedWordVo> update(@RequestBody UpdateBlockedWord request) {
        return Response.ok(service.update(request.id(), request.value()));
    }

    @DeleteMapping("/{id}/enable")
    public Response<Empty> enable(@PathVariable String id) {
        service.enable(id);
        return Response.ok();
    }


    @DeleteMapping("/{id}/disable")
    public Response<Empty> disable(@PathVariable String id) {
        service.disable(id);
        return Response.ok();
    }

    @DeleteMapping("/{id}")
    public Response<Empty> delete(@PathVariable String id) {
        service.delete(id);
        return Response.ok();
    }

    @GetMapping("/all")
    public Response<List<BlockedWordVo>> all() {
        return Response.ok(service.all());
    }

    public record SearchBlockedWord(@Schema(description = "按名字检索") String key,
                                    @Schema(defaultValue = "1", description = "从1开始") Integer pageNo,
                                    @Schema(defaultValue = "20") Integer pageSize) {

        public SearchBlockedWord(String key, Integer pageNo, Integer pageSize) {
            this.key = key;
            this.pageNo = pageNo != null ? pageNo : 1;
            this.pageSize = pageSize != null ? pageSize : 20;
        }
    }

    @PostMapping("/search")
    public Response<Response.PageVo<BlockedWordVo>> search(@RequestBody SearchBlockedWord request) {
        return Response.page(service.search(request));
    }
}
