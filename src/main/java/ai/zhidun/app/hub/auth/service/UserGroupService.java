package ai.zhidun.app.hub.auth.service;

import ai.zhidun.app.hub.auth.controller.UserGroupController.SearchUserGroups;
import ai.zhidun.app.hub.auth.model.UserGroupVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.lang.NonNull;

public interface UserGroupService {

    IPage<UserGroupVo> search(SearchUserGroups request);

    record CreateUserGroup(
            String name,
            String description,
            JsonNode ext,
            String adminId) {

        public @NonNull JsonNode ext() {
            return ext != null ? ext : JsonNodeFactory.instance.objectNode();
        }
    }

    UserGroupVo insert(CreateUserGroup request);

    record UpdateUserGroup(String name, String description, JsonNode ext, String adminId) {

    }

    UserGroupVo update(String id, UpdateUserGroup request);

    void delete(String id);

    void deleteUser(String groupId, String userId);

    void addUser(String groupId, String userId);
}