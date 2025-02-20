package ai.zhidun.app.hub.common;

import com.baomidou.mybatisplus.core.conditions.interfaces.Func;
import io.swagger.v3.oas.annotations.media.Schema;

public enum Sort {
    @Schema(description = "创建时间正序")
    CREATED_AT_ASC,
    @Schema(description = "创建时间倒序")
    CREATED_AT_DESC,
    @Schema(description = "更新时间正序")
    UPDATED_AT_ASC,
    @Schema(description = "更新时间倒序")
    UPDATED_AT_DESC;

    public <Children, R> Children sort(Func<Children, R> query, R createTime, R updateTime) {
        return switch (this) {
            case CREATED_AT_ASC -> query.orderByAsc(createTime);
            case CREATED_AT_DESC -> query.orderByDesc(createTime);
            case UPDATED_AT_ASC -> query.orderByAsc(updateTime);
            case UPDATED_AT_DESC -> query.orderByDesc(updateTime);
        };
    }
}
