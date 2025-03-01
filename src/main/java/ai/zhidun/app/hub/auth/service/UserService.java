package ai.zhidun.app.hub.auth.service;

import ai.zhidun.app.hub.auth.model.UserVo;

public interface UserService {

    String name(String id);

    UserVo getByName(String name);

    @Deprecated
    UserVo create(String name, int permit);
}