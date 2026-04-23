package com.drp.build.enums;

public enum BuildStatus {
    QUEUED(0, "排队中"),
    RUNNING(1, "运行中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败"),
    CANCELLED(4, "已取消"),
    TIMEOUT(5, "超时");

    private final int code;
    private final String description;

    BuildStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static BuildStatus fromCode(int code) {
        for (BuildStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown build status code: " + code);
    }
}
