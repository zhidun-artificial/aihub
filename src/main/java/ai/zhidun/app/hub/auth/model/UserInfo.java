package ai.zhidun.app.hub.auth.model;

import ai.zhidun.app.hub.auth.dao.User;

public record UserInfo(
        String id,
        String name,
        Long createTime,
        Long updateTime
) {
    public static UserInfo from(User user) {
        return new UserInfo(
                user.getId(),
                user.getName(),
                user.getCreateTime().getTime(),
                user.getUpdateTime().getTime());
    }
}
