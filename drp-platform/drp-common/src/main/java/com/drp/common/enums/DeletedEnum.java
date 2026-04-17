package com.drp.common.enums;

/**
 * 逻辑删除状态枚举
 *
 * @author Nick
 */
public enum DeletedEnum {

    /**
     * 未删除
     */
    NOT_DELETED(0, "未删除"),

    /**
     * 已删除
     */
    DELETED(1, "已删除");

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
    DeletedEnum(int code, String description) {
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
    public static DeletedEnum getByCode(int code) {
        for (DeletedEnum deleted : values()) {
            if (deleted.code == code) {
                return deleted;
            }
        }
        return null;
    }
}
