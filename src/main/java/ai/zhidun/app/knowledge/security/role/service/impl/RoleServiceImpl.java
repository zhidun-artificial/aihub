package ai.zhidun.app.knowledge.security.role.service.impl;

import ai.zhidun.app.knowledge.common.BizError;
import ai.zhidun.app.knowledge.common.BizException;
import ai.zhidun.app.knowledge.security.auth.service.JwtSupport;
import ai.zhidun.app.knowledge.security.role.controller.RoleController;
import ai.zhidun.app.knowledge.security.role.dao.*;
import ai.zhidun.app.knowledge.security.role.model.PermissionVo;
import ai.zhidun.app.knowledge.security.role.model.RoleInfo;
import ai.zhidun.app.knowledge.security.role.model.RolePermissions;
import ai.zhidun.app.knowledge.security.role.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final PermissionMapper permissionMapper;

    private final RolePermissionMapper rolePermissionMapper;
    private final RoleMapper roleMapper;

    private final List<Integer> defaultRolePermissionIds;

    public RoleServiceImpl(PermissionMapper permissionMapper, RolePermissionMapper rolePermissionMapper, RoleMapper roleMapper,
                           @Value("${auth.default-role-permissions}" ) List<Integer> defaultRolePermissionIds) {
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.roleMapper = roleMapper;
        this.defaultRolePermissionIds = defaultRolePermissionIds;
    }

    @Override
    public RoleInfo create(String name, String remark) {
        LambdaQueryWrapper<Role> query = Wrappers
                .lambdaQuery(Role.class)
                .eq(Role::getName, name);
        if (super.exists(query)) {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("角色名已经存在!"));
        }

        Role role = new Role();
        role.setName(name);
        role.setRemarks(remark);
        role.setCreator(JwtSupport.userId());
        this.save(role);
        role = this.getById(role.getId());
        return RoleInfo.from(role);
    }

    @Override
    public IPage<RoleInfo> search(RoleController.SearchRoles request) {
        PageDTO<Role> page = new PageDTO<>(request.pageNo(), request.pageSize());

        LambdaQueryWrapper<Role> query = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(request.key())) {
            query = query.like(Role::getName, "%" + request.key() + "%");
        }

        return this
                .page(page, query)
                .convert(RoleInfo::from);
    }

    @Override
    public RolePermissions getPermissions(Integer roleId) {
        List<Permission> permissions = permissionMapper
                .selectList(Wrappers.lambdaQuery(Permission.class));

        Map<Integer, PermissionVo> cache = HashMap.newHashMap(permissions.size());

        for (Permission permission : permissions) {
            cache.put(permission.getId(), PermissionVo.from(permission));
        }

        List<RolePermission> list = rolePermissionMapper.selectList(Wrappers
                .lambdaQuery(RolePermission.class)
                .eq(RolePermission::getRoleId, roleId)
        );

        for (RolePermission permission : list) {
            if (cache.containsKey(permission.getPermissionId())) {
                cache.get(permission.getPermissionId()).setHas(true);
            }
        }

        return new RolePermissions(cache.values());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void putPermissions(Integer roleId, RolePermissions permissions) {
        if (Objects.equals(roleId, adminRolId)) {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("管理员权限不应该修改"));
        }
        rolePermissionMapper.delete(Wrappers
                .lambdaQuery(RolePermission.class)
                .eq(RolePermission::getRoleId, roleId)
        );

        List<RolePermission> list = new ArrayList<>();
        for (PermissionVo datum : permissions.data()) {
            if (datum.isHas()) {
                RolePermission e = new RolePermission();
                e.setRoleId(roleId);
                e.setPermissionId(datum.getId());
                list.add(e);
            }
        }
        rolePermissionMapper.insert(list);
    }

    @Override
    public void delete(Integer id) {
        if (Objects.equals(id, adminRolId)) {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("管理员角色不应该删除"));
        }
        if (Objects.equals(id, defaultRolId)) {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("默认角色不应该删除"));
        }

        LambdaQueryWrapper<RolePermission> queryWrapper = Wrappers.lambdaQuery(RolePermission.class)
                .eq(RolePermission::getRoleId, id);
        rolePermissionMapper.delete(queryWrapper);
        roleMapper.deleteById(id);
    }

    @PostConstruct
    public void init() {
        initAdmin();
        initDefaultRole();
    }

    private Integer defaultRolId;

    private void initDefaultRole() {
        LambdaQueryWrapper<Role> query = Wrappers.lambdaQuery();
        query = query.eq(Role::getName, "default_role");
        if (!this.exists(query)) {
            Role role = new Role();
            role.setName("default_role");
            role.setRemarks("预设默认角色:无法修改");
            role.setCreator(0);
            this.save(role);
            defaultRolId = role.getId();
        } else {
            Role role = this.getOne(query, true);
            defaultRolId = role.getId();
        }

        // clean all permission
        rolePermissionMapper.delete(Wrappers
                .lambdaQuery(RolePermission.class)
                .eq(RolePermission::getRoleId, defaultRolId)
        );

        List<Permission> permissions = permissionMapper
                .selectList(Wrappers.lambdaQuery(Permission.class));

        List<RolePermission> list = new ArrayList<>();
        for (Permission permission : permissions) {
            if (defaultRolePermissionIds.contains(permission.getId())) {
                RolePermission e = new RolePermission();
                e.setRoleId(defaultRolId);
                e.setPermissionId(permission.getId());
                list.add(e);
            }

        }
        rolePermissionMapper.insert(list);
    }

    private Integer adminRolId;

    private void initAdmin() {
        LambdaQueryWrapper<Role> query = Wrappers.lambdaQuery();
        query = query.eq(Role::getName, "admin");
        if (!this.exists(query)) {
            Role role = new Role();
            role.setName("admin");
            role.setRemarks("预设管理员:无法修改");
            role.setCreator(0);
            this.save(role);
            adminRolId = role.getId();
        } else {
            Role role = this.getOne(query, true);
            adminRolId = role.getId();
        }

        // clean all permission
        rolePermissionMapper.delete(Wrappers
                .lambdaQuery(RolePermission.class)
                .eq(RolePermission::getRoleId, adminRolId)
        );


        List<Permission> permissions = permissionMapper
                .selectList(Wrappers.lambdaQuery(Permission.class));

        List<RolePermission> list = new ArrayList<>();
        for (Permission permission : permissions) {
            RolePermission e = new RolePermission();
            e.setRoleId(adminRolId);
            e.setPermissionId(permission.getId());
            list.add(e);

        }
        rolePermissionMapper.insert(list);
    }

    @Override
    public Integer adminRoleId() {
        return adminRolId;
    }

    @Override
    public Integer newUserRoleId() {
        return defaultRolId;
    }

    @Override
    public RoleInfo update(Integer id, String name, String remarks) {
        Role role = this.getById(id);
        if (role == null) {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("角色不存在!"));
        }
        if (StringUtils.isNotBlank(name)) {
            LambdaQueryWrapper<Role> query = Wrappers
                .lambdaQuery(Role.class)
                .eq(Role::getName, name)
                .ne(Role::getId, id);
            if (super.exists(query)) {
                throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("角色名已经存在!"));
            }
        }

        role.setName(name);
        role.setRemarks(remarks);
        this.saveOrUpdate(role);

        role = this.getById(id);
        return RoleInfo.from(role);
    }
}
