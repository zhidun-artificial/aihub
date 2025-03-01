package ai.zhidun.app.hub.auth.service;

public interface UserService {
    int USER = 0;
    int GROUP_ADMIN = 1;
    int SUPER_ADMIN = 2;
    int HIDDEN_USER = -1;

    String name(String id);

}