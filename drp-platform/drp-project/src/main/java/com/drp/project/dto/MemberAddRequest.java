package com.drp.project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 添加项目成员请求对象
 *
 * @author Nick
 */
public class MemberAddRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "角色不能为空")
    @Pattern(regexp = "^(OWNER|DEVELOPER|REPORTER)$", message = "角色值不合法")
    private String role;

    // ==================== Getter & Setter ====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}