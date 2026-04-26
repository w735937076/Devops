package com.drp.deploy.enums;

import lombok.Getter;

@Getter
public enum DeployStatus {
    WAIT_APPROVAL(0, "待审批"),
    PENDING(1, "排队中"),
    RUNNING(2, "执行中"),
    SUCCESS(3, "成功"),
    FAILED(4, "失败"),
    CANCELLED(5, "已取消"),
    ROLLED_BACK(6, "已回滚"),
    REJECTED(7, "已驳回");

    private final int code;
    private final String description;

    DeployStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static DeployStatus fromCode(Integer code) {
        if (code == null) return PENDING;
        for (DeployStatus status : values()) {
            if (status.code == code) return status;
        }
        return PENDING;
    }
}
