package ai.zhidun.app.hub.auth.dao;

import java.util.Date;

public record UserInfo(String id, String name, Integer permit, Date createTime, Date updateTime, String groupId) {
}