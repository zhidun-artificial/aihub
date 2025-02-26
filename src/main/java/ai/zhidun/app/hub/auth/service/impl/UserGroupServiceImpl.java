package ai.zhidun.app.hub.auth.service.impl;

import ai.zhidun.app.hub.auth.controller.UserGroupController.SearchUserGroups;
import ai.zhidun.app.hub.auth.dao.*;
import ai.zhidun.app.hub.auth.model.UserGroupVo;
import ai.zhidun.app.hub.auth.model.UserVo;
import ai.zhidun.app.hub.auth.service.JwtSupport;
import ai.zhidun.app.hub.auth.service.UserGroupService;
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
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static ai.zhidun.app.hub.auth.service.UserService.GROUP_ADMIN;

@Service
public class UserGroupServiceImpl extends ServiceImpl<UserGroupMapper, UserGroup> implements UserGroupService {
    private final UserGroupMapMapper mapMapper;

    private final UserAggMapper aggMapper;

    private final UserService userService;

    private final UserMapper userMapper;

    public UserGroupServiceImpl(UserGroupMapMapper mapMapper, UserAggMapper aggMapper, UserService userService, UserMapper userMapper) {
        this.mapMapper = mapMapper;
        this.aggMapper = aggMapper;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @Override
    public IPage<UserGroupVo> search(SearchUserGroups request) {
        PageDTO<UserGroup> page = new PageDTO<>(request.pageNo(), request.pageSize());

        LambdaQueryWrapper<UserGroup> query = Wrappers
                .lambdaQuery(UserGroup.class)
                .isNotNull(UserGroup::getAlive)
                .like(StringUtils.isNotBlank(request.key()), UserGroup::getName, "%" + request.key() + "%");

        query = request.sort()
                .sort(query, UserGroup::getCreateTime, UserGroup::getUpdateTime);

        IPage<UserGroup> groups = this.page(page, query);

        Map<String, List<UserVo>> userMap = new HashMap<>();

        List<String> ids = new ArrayList<>();
        for (UserGroup group : groups.getRecords()) {
            ids.add(group.getId());
        }

        // add group admin
        for (UserInfo admin : userMapper.selectAdminByGroupIds(ids)) {
            userMap.computeIfAbsent(admin.groupId(), k -> new ArrayList<>())
                    .add(UserVo.from(admin, GROUP_ADMIN));
        }

        Map<String, Integer> countMap = new HashMap<>();

        if (!ids.isEmpty()) {
            LambdaQueryWrapper<UserAgg> wrapper = Wrappers
                    .lambdaQuery(UserAgg.class)
                    .select(UserAgg::getGroupId, UserAgg::getCount)
                    .in(UserAgg::getGroupId, ids)
                    .groupBy(UserAgg::getGroupId);

            for (UserAgg agg : aggMapper.selectList(wrapper)) {
                countMap.put(agg.getGroupId(), agg.getCount());
            }
        }

        if (!ids.isEmpty()) {

            //todo here only superAdmin can see all users
            for (UserInfo user : userMapper.selectByGroupIds(ids)) {
                userMap.computeIfAbsent(user.groupId(), k -> new ArrayList<>())
                        .add(UserVo.from(user));
            }
        }

        return groups.convert(vo -> this.from(vo,
                countMap.getOrDefault(vo.getId(), 0),
                userMap.getOrDefault(vo.getId(), List.of())));
    }

    private static final JsonMapper jsonMapper = new JsonMapper();

    @SneakyThrows
    private UserGroupVo from(UserGroup vo, int userCount, List<UserVo> users) {
        return new UserGroupVo(vo.getId(),
                vo.getName(),
                userService.name(vo.getCreator()),
                vo.getDescription(),
                jsonMapper.readTree(vo.getExt()),
                vo.getCreateTime().getTime(),
                vo.getUpdateTime().getTime(),
                userCount,
                users
        );
    }

    @SneakyThrows
    private UserGroupVo from(UserGroup vo, int userCount) {
        return from(vo, userCount, Collections.emptyList());
    }

    @Override
    public UserGroupVo insert(CreateUserGroup request) {
        LambdaQueryWrapper<UserGroup> query = Wrappers
                .lambdaQuery(UserGroup.class)
                .eq(UserGroup::getName, request.name());

        if (super.exists(query)) {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("同名的组织已经存在!"));
        }

        UserGroup entity = new UserGroup();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setExt(request.ext().toString());
        entity.setAdminId(request.adminId());
        entity.setCreator(JwtSupport.userId());
        this.save(entity);

        entity = this.getById(entity.getId());

        return from(entity, 0);
    }

    @Override
    public UserGroupVo update(String id, UpdateUserGroup request) {
        if (this.getById(id) instanceof UserGroup group) {
            if (request.name() != null) {
                group.setName(request.name());
            }
            if (request.description() != null) {
                group.setDescription(request.description());
            }
            if (request.ext() != null) {
                group.setExt(request.ext().toString());
            }
            if (request.adminId() != null) {
                group.setAdminId(request.adminId());
            }
            this.saveOrUpdate(group);

            LambdaQueryWrapper<UserGroupMap> wrapper = Wrappers
                    .lambdaQuery(UserGroupMap.class)
                    .eq(UserGroupMap::getGroupId, id);

            Long count = mapMapper.selectCount(wrapper);

            group = this.getById(group.getId());

            return from(group, Math.toIntExact(count));
        } else {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("组织不存在!"));
        }
    }

    @Override
    public void delete(String id) {
        LambdaUpdateWrapper<UserGroup> update = Wrappers.lambdaUpdate(UserGroup.class)
                .eq(UserGroup::getId, id)
                .set(UserGroup::getAlive, null);
        super.update(update);
    }

    @Override
    @Transactional
    public void deleteUser(String groupId, String userId) {
        checkPermission(groupId);

        mapMapper.delete(Wrappers
                .lambdaQuery(UserGroupMap.class)
                .eq(UserGroupMap::getGroupId, groupId)
                .eq(UserGroupMap::getUserId, userId)
        );
    }

    private void checkPermission(String groupId) {
        UserGroup target = this.getById(groupId);
        if (target == null) {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("组织不存在!"));
        }
        String currentUserId = JwtSupport.userId();
        // todo add superAdmin check
        if (!target.getCreator().equals(currentUserId)) {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("无操作权限!"));
        }
    }

    @Override
    @Transactional
    public void addUser(String groupId, String userId) {
        checkPermission(groupId);

        LambdaQueryWrapper<UserGroupMap> wrapper = Wrappers
                .lambdaQuery(UserGroupMap.class)
                .eq(UserGroupMap::getGroupId, groupId)
                .eq(UserGroupMap::getUserId, userId);
        if (!mapMapper.exists(wrapper)) {
            UserGroupMap entity = new UserGroupMap();
            entity.setGroupId(groupId);
            entity.setUserId(userId);
            mapMapper.insert(entity);
        }

    }
}
