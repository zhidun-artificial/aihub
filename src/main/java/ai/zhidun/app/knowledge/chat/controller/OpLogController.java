package ai.zhidun.app.knowledge.chat.controller;

import ai.zhidun.app.knowledge.chat.client.DifyConversationClient.SortBy;
import ai.zhidun.app.knowledge.chat.model.OpLogVo;
import ai.zhidun.app.knowledge.chat.service.OpLogService;
import ai.zhidun.app.knowledge.common.Response;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "对话历史记录", description = "对话历史记录相关接口")
@SecurityRequirement(name = "auth")
@RestController
@RequestMapping("/api/v1/oplogs")
public class OpLogController {

    private final OpLogService service;

    public OpLogController(OpLogService service) {
        this.service = service;
    }

    public record SearchOplogs(
            @Schema(description = "按名字检索")
            String key,
            @Schema(description = "排序，默认创建时间倒序", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            SortBy sort,
            @Schema(description = "开始时间，毫秒时间戳")
            Long begin,
            @Schema(description = "结束时间，毫秒时间戳")
            Long end,
            @Schema(defaultValue = "1", description = "从1开始")
            Integer pageNo,
            @Schema(defaultValue = "20")
            Integer pageSize) {

        public SearchOplogs(String key, SortBy sort, Long begin, Long end, Integer pageNo, Integer pageSize) {
            this.key = key;
            this.sort = sort != null ? sort : SortBy.CREATED_AT_DESC;
            this.begin = begin;
            this.end = end;
            this.pageNo = pageNo != null ? pageNo : 1;
            this.pageSize = pageSize != null ? pageSize : 20;
        }
    }

    @PostMapping("/search")
    public Response<Response.PageVo<OpLogVo>> search(@RequestBody SearchOplogs request) {
        return Response.page(service.search(request));
    }
}
