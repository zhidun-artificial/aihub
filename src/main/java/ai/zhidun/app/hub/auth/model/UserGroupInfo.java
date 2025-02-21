package ai.zhidun.app.hub.auth.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonNode;

public record UserGroupInfo(
        String id,
        String name,
        int userCount,
        String creatorName,
        String description,
        @JsonUnwrapped
        JsonNode ext,
        Long createTime,
        Long updateTime
) {
}
