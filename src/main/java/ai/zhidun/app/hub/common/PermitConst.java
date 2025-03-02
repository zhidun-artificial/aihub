package ai.zhidun.app.hub.common;


public final class PermitConst {

    /// 用户分为 0: 普通用户, 1: 部门管理员 2: 系统管理员 -1: 特殊用户
    /// 系统管理员 拥有所有权限
    /// 部门管理员 只能管理部门内的资源 助手 和 知识库，部门内用户
    /// 特殊用户 和 普通用户 没有太多区别， 只是 部门管理员看不到用户
    public static final int USER = 0;
    public static final int GROUP_ADMIN = 1;
    public static final int SUPER_ADMIN = 2;
    public static final int HIDDEN_USER = -1;

    ///  助手 和 知识库 分三个级别
    /// 公共 所有人可以使用
    /// 个人 只能创建者看到
    /// 部门内 部门的成员可以看
    public static final int PUBLIC_RESOURCE = 0;
    public static final int PERSONAL_RESOURCE = 1;
    public static final int GROUP_RESOURCE = 2;
}
