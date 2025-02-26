package ai.zhidun.app.hub.auth.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record UserGroupVo(
        String id,
        String name,
        String creatorName,
        String description,
        JsonNode ext,
        Long createTime,
        Long updateTime,
        int userCount,
        List<UserVo> users
) {
}
