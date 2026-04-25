package com.drp.notify.enums;

public enum NotificationChannelType {
    WECOM("企业微信"),
    DINGTALK("钉钉"),
    FEISHU("飞书"),
    EMAIL("邮件");

    private final String description;

    NotificationChannelType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
