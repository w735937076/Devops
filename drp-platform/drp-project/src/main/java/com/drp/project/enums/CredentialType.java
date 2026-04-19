package com.drp.project.enums;

/**
 * 凭证类型枚举
 *
 * @author Nick
 */
public enum CredentialType {

    USERNAME_PASSWORD("用户名密码"),
    ACCESS_TOKEN("访问令牌"),
    SSH_KEY("SSH 私钥");

    private final String description;

    CredentialType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (CredentialType type : values()) {
            if (type.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
