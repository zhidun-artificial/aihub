package ai.zhidun.app.hub.auth.service;

import ai.zhidun.app.hub.auth.controller.UserController.SearchUsers;
import ai.zhidun.app.hub.auth.model.UserVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    int USER = 0;
    int GROUP_ADMIN = 1;
    int SUPER_ADMIN = 2;
    int HIDDEN_USER = -1;


    IPage<UserVo> search(SearchUsers request);

    UserVo register(String username, String password);

    void delete(String id);

    record LoginResult(UserVo info, String token) {

    }

    LoginResult login(String username, String password);

    String name(String id);

    int importByXlsx(MultipartFile file);
}