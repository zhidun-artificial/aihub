package ai.zhidun.app.hub.auth.service;

import ai.zhidun.app.hub.auth.controller.UserGroupController.SearchUserGroups;
import ai.zhidun.app.hub.auth.model.UserGroupInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.JsonNode;

public interface UserGroupService {

    IPage<UserGroupInfo> search(SearchUserGroups request);

    UserGroupInfo insert(String name, String description, JsonNode ext);

    UserGroupInfo update(String id, String description, JsonNode ext);

    void delete(String id);
}