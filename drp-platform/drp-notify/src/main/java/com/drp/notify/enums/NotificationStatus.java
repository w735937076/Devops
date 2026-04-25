package com.drp.notify.enums;

public enum NotificationStatus {
    PENDING("待发送"),
    SUCCESS("成功"),
    FAIL("失败"),
    RETRY("重试中");

    private final String description;

    NotificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
