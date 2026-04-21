package com.drp.project.enums;

/**
 * 项目成员角色枚举
 *
 * @author Nick
 */
public enum MemberRole {

    OWNER("所有者", "项目配置、成员与策略管理"),
    DEVELOPER("开发者", "可维护环境变量并触发测试环境发布"),
    REPORTER("观察者", "只读访问，不能修改配置");

    private final String description;
    private final String permission;

    MemberRole(String description, String permission) {
        this.description = description;
        this.permission = permission;
    }

    public String getDescription() {
        return description;
    }

    public String getPermission() {
        return permission;
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (MemberRole role : values()) {
            if (role.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}