package ai.zhidun.app.knowledge.common;

import io.swagger.v3.oas.annotations.media.Schema;

public enum Sort {
    @Schema(description = "创建时间正序")
    CREATED_AT_ASC,
    @Schema(description = "创建时间倒序")
    CREATED_AT_DESC,
    @Schema(description = "更新时间正序")
    UPDATED_AT_ASC,
    @Schema(description = "更新时间倒序")
    UPDATED_AT_DESC
}
