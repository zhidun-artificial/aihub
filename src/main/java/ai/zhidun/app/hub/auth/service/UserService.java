package ai.zhidun.app.hub.auth.service;

import ai.zhidun.app.hub.auth.controller.UserController.SearchUsers;
import ai.zhidun.app.hub.auth.model.UserInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    IPage<UserInfo> search(SearchUsers request);

    UserInfo register(String username, String password);

    void delete(String id);

    record LoginResult(UserInfo info, String token) {

    }

    LoginResult login(String username, String password);

    String name(String id);

    int importByXlsx(MultipartFile file);
}