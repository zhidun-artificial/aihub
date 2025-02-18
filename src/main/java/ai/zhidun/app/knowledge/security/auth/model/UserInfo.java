package ai.zhidun.app.knowledge.security.auth.model;

import ai.zhidun.app.knowledge.security.auth.dao.User;

public record UserInfo(
        Integer id,
        String name,
        Integer roleId,
        Long createTime
) {
    public static UserInfo from(User user) {
        return new UserInfo(user.getId(), user.getName(), user.getRoleId(), user.getCreateTime().getTime());
    }
}
