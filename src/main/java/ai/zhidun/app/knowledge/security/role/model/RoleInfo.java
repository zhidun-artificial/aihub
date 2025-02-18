package ai.zhidun.app.knowledge.security.role.model;

import ai.zhidun.app.knowledge.security.role.dao.Role;

public record RoleInfo(
        Integer id,
        String name,
        String remarks,
        long createTime
) {

    public static RoleInfo from(Role role) {
        return new RoleInfo(role.getId(), role.getName(), role.getRemarks(), role.getCreateTime().getTime());
    }
}
