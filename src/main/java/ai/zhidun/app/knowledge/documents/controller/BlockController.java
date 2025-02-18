package ai.zhidun.app.knowledge.documents.controller;

import ai.zhidun.app.knowledge.common.Response;
import ai.zhidun.app.knowledge.common.Response.Empty;
import ai.zhidun.app.knowledge.common.Response.PageVo;
import ai.zhidun.app.knowledge.documents.model.BlockRuleVo;
import ai.zhidun.app.knowledge.documents.service.BlockService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "屏蔽管理", description = "屏蔽相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/blocks")
public class BlockController {

    private final BlockService service;

    public BlockController(BlockService service) {
        this.service = service;
    }

    public record CreateBlock(String blockedWord) {

    }

    @PostMapping
    public Response<BlockRuleVo> create(@RequestBody CreateBlock request) {
        return Response.ok(service.create(request.blockedWord()));
    }

    public record UpdateBlock(String id, String blockedWord) {

    }

    @PutMapping
    public Response<BlockRuleVo> update(@RequestBody UpdateBlock request) {
        return Response.ok(service.update(request.id(), request.blockedWord()));
    }

    @DeleteMapping("/{id}")
    public Response<Empty> delete(@PathVariable Integer id) {
        service.delete(id);
        return Response.ok();
    }

    @GetMapping("/all")
    public Response<List<BlockRuleVo>> all() {
        return Response.ok(service.all());
    }

    public record SearchBlock(
            @Schema(description = "按名字检索")
            String key,
            @Schema(defaultValue = "1", description = "从1开始")
            Integer pageNo,
            @Schema(defaultValue = "20")
            Integer pageSize) {

        public SearchBlock(String key, Integer pageNo, Integer pageSize) {
            this.key = key;
            this.pageNo = pageNo != null ? pageNo : 1;
            this.pageSize = pageSize != null ? pageSize : 20;
        }
    }

    @PostMapping("/search")
    public Response<PageVo<BlockRuleVo>> search(@RequestBody SearchBlock request) {
        return Response.page(service.search(request));
    }
}
