package ai.zhidun.app.knowledge.security.role.model;

import ai.zhidun.app.knowledge.security.role.dao.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionVo {
    private int id;
    private Integer parentId;
    private String name;
    private String title;
    private boolean has;

    public static PermissionVo from(Permission permission) {
        return new PermissionVo(permission.getId(), permission.getParentId(), permission.getName(), permission.getTitle(), false);
    }
}
