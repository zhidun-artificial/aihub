package ai.zhidun.app.hub.auth.model;

import ai.zhidun.app.hub.auth.dao.User;
import ai.zhidun.app.hub.auth.dao.UserInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserVo(
        String id,
        String name,
        @Schema(description = "0: 普通用户, 1: 部门管理员 2: 系统管理员 -1: 特殊用户")
        Integer permit,
        Long createTime,
        Long updateTime
) {

    public static UserVo from(User user) {
        return new UserVo(
                user.getId(),
                user.getName(),
                user.getPermit(),
                user.getCreateTime().getTime(),
                user.getUpdateTime().getTime());
    }

    public static UserVo from(UserInfo userInfo) {
        return new UserVo(
                userInfo.id(),
                userInfo.name(),
                userInfo.permit(),
                userInfo.createTime().getTime(),
                userInfo.updateTime().getTime());
    }

    public static UserVo from(UserInfo userInfo, Integer permit) {
        return new UserVo(
                userInfo.id(),
                userInfo.name(),
                permit,
                userInfo.createTime().getTime(),
                userInfo.updateTime().getTime());
    }
}
