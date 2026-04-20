package com.drp.project.enums;

/**
 * 项目类型枚举
 *
 * @author Nick
 */
public enum ProjectType {

    JAVA_MAVEN("Java Maven"),
    NODE("Node.js"),
    PYTHON("Python");

    private final String description;

    ProjectType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (ProjectType type : values()) {
            if (type.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}