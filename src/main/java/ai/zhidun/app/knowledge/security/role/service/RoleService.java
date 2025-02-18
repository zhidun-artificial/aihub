package ai.zhidun.app.knowledge.security.role.service;

import ai.zhidun.app.knowledge.security.role.controller.RoleController.SearchRoles;
import ai.zhidun.app.knowledge.security.role.model.RoleInfo;
import ai.zhidun.app.knowledge.security.role.model.RolePermissions;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface RoleService  {

    RoleInfo create(String name, String remark);

    IPage<RoleInfo> search(SearchRoles request);

    RolePermissions getPermissions(Integer id);

    void putPermissions(Integer id, RolePermissions permissions);

    void delete(Integer id);

    Integer adminRoleId();

    Integer newUserRoleId();

    RoleInfo update(Integer id, String name, String remarks);
}
