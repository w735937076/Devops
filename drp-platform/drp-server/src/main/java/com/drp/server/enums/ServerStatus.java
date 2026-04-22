package com.drp.server.enums;

public enum ServerStatus {
    OFFLINE(0, "离线"),
    ONLINE(1, "在线"),
    BUSY(2, "忙碌");

    private final int value;
    private final String desc;

    ServerStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static ServerStatus fromValue(int value) {
        for (ServerStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return OFFLINE;
    }
}
