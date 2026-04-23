package com.drp.build.enums;

public enum TriggerType {
    MANUAL("手动触发"),
    PUSH("代码推送"),
    TAG("标签创建"),
    SCHEDULE("定时任务"),
    API("API触发");

    private final String description;

    TriggerType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
