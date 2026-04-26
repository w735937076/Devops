package com.drp.deploy.enums;

import lombok.Getter;

@Getter
public enum RiskLevel {
    LOW("LOW", "低"),
    MEDIUM("MEDIUM", "中"),
    HIGH("HIGH", "高");

    private final String code;
    private final String description;

    RiskLevel(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static RiskLevel fromCode(String code) {
        if (code == null || code.isBlank()) return LOW;
        for (RiskLevel level : values()) {
            if (level.code.equalsIgnoreCase(code)) return level;
        }
        return LOW;
    }
}
