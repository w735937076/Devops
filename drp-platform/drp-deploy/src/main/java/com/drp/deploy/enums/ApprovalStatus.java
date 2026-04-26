package com.drp.deploy.enums;

import lombok.Getter;

@Getter
public enum ApprovalStatus {
    NOT_REQUIRED("NOT_REQUIRED", "不需要"),
    PENDING("PENDING", "待审批"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已驳回");

    private final String code;
    private final String description;

    ApprovalStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ApprovalStatus fromCode(String code) {
        if (code == null || code.isBlank()) return NOT_REQUIRED;
        for (ApprovalStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) return status;
        }
        return NOT_REQUIRED;
    }
}
