package com.drp.project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 更新项目成员请求对象
 *
 * @author Nick
 */
public class MemberUpdateRequest {

    @NotNull(message = "角色不能为空")
    @Pattern(regexp = "^(OWNER|DEVELOPER|REPORTER)$", message = "角色值不合法")
    private String role;

    // ==================== Getter & Setter ====================

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}