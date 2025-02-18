package ai.zhidun.app.knowledge.security.auth.service;

import ai.zhidun.app.knowledge.security.auth.controller.UserController.SearchUsers;
import ai.zhidun.app.knowledge.security.auth.model.UserInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    IPage<UserInfo> search(SearchUsers request);

    UserInfo register(String username, String password);

    void updateRole(Integer id, Integer role);

    void updateSelfPassword(String oldPassword, String newPassword);

    void resetPassword(Integer id);

    void delete(Integer id);

    record LoginResult(UserInfo info, String token) {

    }

    LoginResult login(String username, String password);

    String name(Integer id);

    Integer adminUserId();

    int importByXlsx(MultipartFile file);
}