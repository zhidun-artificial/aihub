package ai.zhidun.app.knowledge.security.auth.service.impl;

import ai.zhidun.app.knowledge.common.BizError;
import ai.zhidun.app.knowledge.common.BizException;
import ai.zhidun.app.knowledge.security.auth.controller.UserController.SearchUsers;
import ai.zhidun.app.knowledge.security.auth.dao.User;
import ai.zhidun.app.knowledge.security.auth.dao.UserMapper;
import ai.zhidun.app.knowledge.security.auth.model.UserInfo;
import ai.zhidun.app.knowledge.security.auth.service.JwtService;
import ai.zhidun.app.knowledge.security.auth.service.JwtSupport;
import ai.zhidun.app.knowledge.security.auth.service.UserService;
import ai.zhidun.app.knowledge.security.role.service.RoleService;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtService jwt;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private final RoleService roleService;

    @Value("${auth.default-password}")
    private String defaultPassword;

    public UserServiceImpl(JwtService jwt, RoleService roleService) {
        this.jwt = jwt;
        this.roleService = roleService;
    }

    public IPage<UserInfo> search(SearchUsers request) {
        PageDTO<User> page = new PageDTO<>(request.pageNo(), request.pageSize());

        LambdaQueryWrapper<User> query = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(request.key())) {
            query = query.like(User::getName, "%" + request.key() + "%");
        }

        query = switch (request.sort()) {
            case CREATED_AT_ASC -> query.orderByAsc(User::getRegisterTime);
            case CREATED_AT_DESC -> query.orderByDesc(User::getRegisterTime);
            case UPDATED_AT_ASC -> query.orderByAsc(User::getUpdateTime);
            case UPDATED_AT_DESC -> query.orderByDesc(User::getUpdateTime);
        };


        return this
                .page(page, query)
                .convert(UserInfo::from);
    }

    @Override
    public UserInfo register(String username, String password) {
        String encodedPassword = encoder.encode(password);

        LambdaQueryWrapper<User> query = Wrappers
                .lambdaQuery(User.class)
                .eq(User::getName, username);
        if (super.exists(query)) {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("用户名已经存在!"));
        }

        User entity = new User();
        entity.setName(username);
        entity.setPassword(encodedPassword);
        entity.setRegisterTime(Date.from(Instant.now()));
        entity.setRoleId(roleService.newUserRoleId());
        if (super.save(entity)) {
            entity = super.getById(entity.getId());
            return UserInfo.from(entity);
        } else {
            throw new BizException(HttpStatus.INTERNAL_SERVER_ERROR, BizError.error("插入记录失败!"));
        }

    }

    private Integer adminId;

    @PostConstruct
    public void init() {
        LambdaQueryWrapper<User> query = Wrappers.lambdaQuery();
        query = query.eq(User::getName, "admin");
        if (!this.exists(query)) {
            UserInfo info = register("admin", "goodluck.zhidun");
            this.adminId = info.id();
        } else {
            User user = this.getOne(query, true);
            this.adminId = user.getId();
        }

        if (super.getById(adminId) instanceof User user) {
            user.setRoleId(roleService.adminRoleId());
            super.updateById(user);
        } else {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("用户名不存在??"));
        }
    }

    @Override
    public void updateRole(Integer userId, Integer role) {
        if (Objects.equals(adminId, userId)) {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("管理员角色不应该修改"));
        }

        if (super.getById(userId) instanceof User user) {
            user.setRoleId(role);
            super.updateById(user);
        } else {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("用户名不存在??"));
        }
    }

    @Override
    public void updateSelfPassword(String oldPassword, String newPassword) {
        int userId = JwtSupport.userId();
        if (super.getById(userId) instanceof User user) {
            if (encoder.matches(oldPassword, user.getPassword())) {
                String encodedPassword = encoder.encode(newPassword);
                user.setPassword(encodedPassword);
                super.updateById(user);

            } else {
                throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("旧密码不匹配!"));
            }

        } else {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("当前用户名不存在??"));
        }
    }

    @Override
    public void resetPassword(Integer id) {
        if (super.getById(id) instanceof User user) {
            String encodedPassword = encoder.encode(defaultPassword);
            user.setPassword(encodedPassword);
            super.updateById(user);
        } else {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("用户名不存在??"));
        }

    }

    @Override
    public void delete(Integer id) {
        super.removeById(id);
    }

    @Override
    public LoginResult login(String username, String password) {
        LambdaQueryWrapper<User> query = Wrappers
                .lambdaQuery(User.class)
                .eq(User::getName, username);

        if (this.getOne(query) instanceof User user) {
            if (encoder.matches(password, user.getPassword())) {
                String token = jwt.encode(JwtService.ClaimInfo.common(user.getName(), user.getId()));
                user.setLastLoginTime(Date.from(Instant.now()));
                this.updateById(user);
                return new LoginResult(UserInfo.from(user), token);

            } else {
                throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("用户名密码不匹配!"));
            }
        } else {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("用户名不存在!"));
        }
    }

    @Cacheable(value = "username", key = "#p0")
    @Override
    public String name(Integer id) {
        if (this.getById(id) instanceof User user) {
            return user.getName();
        } else {
            return "未知";
        }
    }

    @Override
    public Integer adminUserId() {
        return adminId;
    }

    @SneakyThrows
    @Override
    public int importByXlsx(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            UserDataImport listener = new UserDataImport(this);
            EasyExcel
                    .read(in, UserData.class, listener)
                    .doReadAll();
            return listener.count();
        }
    }

    public int save(List<UserData> cache) {
        // batch save
        List<User> users = new ArrayList<>(cache.size());

        for (UserData data : cache) {
            String encodedPassword;
            if (StringUtils.isNotBlank(data.getPassword())) {
                encodedPassword = encoder.encode(data.getPassword());
            } else {
                encodedPassword = encoder.encode(defaultPassword);
            }
            User entity = new User();
            entity.setName(data.getUsername());
            entity.setPassword(encodedPassword);
            entity.setRegisterTime(Date.from(Instant.now()));
            entity.setRoleId(roleService.newUserRoleId());
            users.add(entity);
        }
        return this.getBaseMapper().insertIgnoreBatchSomeColumn4Mysql(users);
    }
}
