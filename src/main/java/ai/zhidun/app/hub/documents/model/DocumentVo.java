package ai.zhidun.app.hub.documents.model;

import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentVo(
    String id,
    String baseId,
    String title,
    String fileName,
    @Schema(description = """
        文档状态:
        STATUS_PENDING = 0   - 文档等待处理
        STATUS_INGESTING = 1 - 文档正在被导入
        STATUS_FINISHED = 2  - 文档处理成功完
        STATUS_ERROR = 3    - 文档处理失败
        """)
    Integer status,
    String url,
    String creator,
    String creatorName,
    String blockedReason,
    long createTime,
    long updateTime) {

}
