package ai.zhidun.app.hub.auth.service.impl;

import ai.zhidun.app.hub.auth.controller.UserController.SearchUsers;
import ai.zhidun.app.hub.auth.dao.User;
import ai.zhidun.app.hub.auth.dao.UserGroupMap;
import ai.zhidun.app.hub.auth.dao.UserGroupMapMapper;
import ai.zhidun.app.hub.auth.dao.UserMapper;
import ai.zhidun.app.hub.auth.model.UserVo;
import ai.zhidun.app.hub.auth.service.JwtService;
import ai.zhidun.app.hub.auth.service.UserService;
import ai.zhidun.app.hub.common.BizError;
import ai.zhidun.app.hub.common.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserGroupMapMapper mapMapper;

    private final JwtService jwt;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${auth.default-password}")
    private String defaultPassword;

    public UserServiceImpl(UserGroupMapMapper mapMapper, JwtService jwt) {
        this.mapMapper = mapMapper;
        this.jwt = jwt;
    }

    public IPage<UserVo> search(SearchUsers request) {
        PageDTO<User> page = new PageDTO<>(request.pageNo(), request.pageSize());

        LambdaQueryWrapper<User> query = Wrappers
                .lambdaQuery(User.class)
                .isNotNull(User::getAlive)
                .like(StringUtils.isNotBlank(request.key()), User::getName, "%" + request.key() + "%");


        if (StringUtils.isNotBlank(request.groupId())) {
            List<String> ids = new ArrayList<>();

            for (UserGroupMap map : mapMapper.selectList(Wrappers.lambdaQuery(UserGroupMap.class)
                    .select(UserGroupMap::getUserId)
                    .eq(UserGroupMap::getGroupId, request.groupId()))) {
                ids.add(map.getUserId());
            }

            if (!ids.isEmpty()) {
                query = query.in(User::getId, ids);
            }
        }

        query = request.sort()
                .sort(query, User::getCreateTime, User::getUpdateTime);

        return this
                .page(page, query)
                .convert(UserVo::from);
    }

    @Override
    public UserVo register(String username, String password) {
        LambdaQueryWrapper<User> query = Wrappers
                .lambdaQuery(User.class)
                .eq(User::getName, username);
        if (super.exists(query)) {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("用户名已经存在!"));
        }

        User entity = new User();
        entity.setName(username);
        if (super.save(entity)) {
            entity = super.getById(entity.getId());
            return UserVo.from(entity);
        } else {
            throw new BizException(HttpStatus.INTERNAL_SERVER_ERROR, BizError.error("插入记录失败!"));
        }

    }

    @Override
    public void delete(String id) {
        LambdaUpdateWrapper<User> update = Wrappers.lambdaUpdate(User.class)
                .eq(User::getId, id)
                .set(User::getAlive, null);

        super.update(update);
    }

    @Override
    public LoginResult login(String username, String password) {
        LambdaQueryWrapper<User> query = Wrappers
                .lambdaQuery(User.class)
                .eq(User::getName, username);

        if (this.getOne(query) instanceof User user) {
            String token = jwt.encode(JwtService.ClaimInfo.common(user.getName(), user.getId()));
            user.setLastLoginTime(Date.from(Instant.now()));
            this.updateById(user);
            return new LoginResult(UserVo.from(user), token);
        } else {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("用户名不存在!"));
        }
    }

    @Cacheable(value = "username", key = "#p0")
    @Override
    public String name(String id) {
        if (this.getById(id) instanceof User user) {
            return user.getName();
        } else {
            return "未知";
        }
    }

    @SneakyThrows
    @Override
    public int importByXlsx(MultipartFile file) {
        //todo
        throw new RuntimeException("Not Implemented");
    }
}
