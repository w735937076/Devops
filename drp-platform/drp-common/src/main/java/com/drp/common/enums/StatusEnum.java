package com.drp.common.enums;

/**
 * 通用状态枚举
 *
 * @author Nick
 */
public enum StatusEnum {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 启用
     */
    ENABLED(1, "启用"),

    /**
     * 删除
     */
    DELETED(2, "已删除");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 构造函数
     */
    StatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据状态码获取枚举
     */
    public static StatusEnum getByCode(int code) {
        for (StatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return this == ENABLED;
    }

    /**
     * 判断是否禁用
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }

    /**
     * 判断是否已删除
     */
    public boolean isDeleted() {
        return this == DELETED;
    }
}
