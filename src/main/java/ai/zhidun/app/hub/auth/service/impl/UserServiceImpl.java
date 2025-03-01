package ai.zhidun.app.hub.auth.service.impl;

import ai.zhidun.app.hub.auth.dao.User;
import ai.zhidun.app.hub.auth.dao.UserMapper;
import ai.zhidun.app.hub.auth.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Cacheable(value = "username", key = "#p0")
    @Override
    public String name(String id) {
        if (this.getById(id) instanceof User user) {
            return user.getName();
        } else {
            return "未知";
        }
    }

}
