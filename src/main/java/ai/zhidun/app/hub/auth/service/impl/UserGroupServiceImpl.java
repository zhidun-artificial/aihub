package ai.zhidun.app.hub.auth.service.impl;

import ai.zhidun.app.hub.auth.controller.UserGroupController.SearchUserGroups;
import ai.zhidun.app.hub.auth.dao.*;
import ai.zhidun.app.hub.auth.model.UserGroupInfo;
import ai.zhidun.app.hub.auth.model.UserInfo;
import ai.zhidun.app.hub.auth.service.JwtService;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserGroupServiceImpl  extends ServiceImpl<UserGroupMapper, UserGroup>  implements UserGroupService {
    private final UserGroupMapMapper mapMapper;

    private final UserAggMapper aggMapper;

    private final UserService userService;

    public UserGroupServiceImpl(UserGroupMapMapper mapMapper, UserAggMapper aggMapper, UserService userService) {
        this.mapMapper = mapMapper;
        this.aggMapper = aggMapper;
        this.userService = userService;
    }

    @Override
    public IPage<UserGroupInfo> search(SearchUserGroups request) {
        PageDTO<UserGroup> page = new PageDTO<>(request.pageNo(), request.pageSize());

        LambdaQueryWrapper<UserGroup> query = Wrappers
                .lambdaQuery(UserGroup.class)
                .isNotNull(UserGroup::getAlive)
                .like(StringUtils.isNotBlank(request.key()), UserGroup::getName, "%" + request.key() + "%");

        query = request.sort()
                .sort(query, UserGroup::getCreateTime, UserGroup::getUpdateTime);

        IPage<UserGroup> groups = this.page(page, query);

        List<String> ids = new ArrayList<>();
        for (UserGroup group : groups.getRecords()) {
            ids.add(group.getId());
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

        return groups.convert(vo -> this.from(vo, countMap.getOrDefault(vo.getId(), 0)));
    }

    private static final JsonMapper jsonMapper = new JsonMapper();

    @SneakyThrows
    private UserGroupInfo from(UserGroup vo, int userCount) {
        return new UserGroupInfo(vo.getId(),
                vo.getName(),
                userCount,
                userService.name(vo.getCreator()),
                vo.getDescription(),
                jsonMapper.readTree(vo.getExt()),
                vo.getCreateTime().getTime(),
                vo.getUpdateTime().getTime()
        );
    }

    @Override
    public UserGroupInfo insert(String name, String description, JsonNode ext) {
        LambdaQueryWrapper<UserGroup> query = Wrappers
                .lambdaQuery(UserGroup.class)
                .eq(UserGroup::getName, name);

        if (super.exists(query)) {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("同名的组织已经存在!"));
        }

        UserGroup entity = new UserGroup();
        entity.setName(name);
        entity.setDescription(description);
        entity.setExt(ext.toString());
        entity.setCreator(JwtSupport.userId());
        this.save(entity);
        return from(entity, 0);
    }

    @Override
    public UserGroupInfo update(String id, String description, JsonNode ext) {
        if (this.getById(id) instanceof UserGroup group) {
            if (description != null) {
                group.setDescription(description);
            }
            if (ext != null) {
                group.setExt(ext.toString());
            }
            group.setCreator(JwtSupport.userId());
            this.saveOrUpdate(group);

            LambdaQueryWrapper<UserGroupMap> wrapper = Wrappers
                    .lambdaQuery(UserGroupMap.class)
                    .eq(UserGroupMap::getGroupId, id);

            Long count = mapMapper.selectCount(wrapper);

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
}
