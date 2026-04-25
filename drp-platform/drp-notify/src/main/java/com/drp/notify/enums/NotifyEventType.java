package com.drp.notify.enums;

public enum NotifyEventType {
    // 构建事件
    BUILD_START("构建开始", "BUILD"),
    BUILD_SUCCESS("构建成功", "BUILD"),
    BUILD_FAIL("构建失败", "BUILD"),

    // 部署事件
    DEPLOY_START("部署开始", "DEPLOY"),
    DEPLOY_SUCCESS("部署成功", "DEPLOY"),
    DEPLOY_FAIL("部署失败", "DEPLOY"),
    DEPLOY_APPROVAL("待审批", "DEPLOY"),

    // 回滚事件
    ROLLBACK_SUCCESS("回滚成功", "ROLLBACK"),
    ROLLBACK_FAIL("回滚失败", "ROLLBACK"),

    // 服务器事件
    SERVER_OFFLINE("服务器离线", "SERVER"),
    SERVER_ALERT("服务器告警", "SERVER"),

    // 安全事件
    LOGIN_FAILED("登录失败", "SECURITY"),
    PERMISSION_DENIED("权限异常", "SECURITY");

    private final String description;
    private final String category;

    NotifyEventType(String description, String category) {
        this.description = description;
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }
}
