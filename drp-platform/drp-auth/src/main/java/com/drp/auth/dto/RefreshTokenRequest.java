package com.drp.auth.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 刷新 Token 请求 DTO
 *
 * @author Nick
 */
public class RefreshTokenRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Refresh Token
     */
    @NotBlank(message = "Refresh Token 不能为空")
    private String refreshToken;

    // ==================== 构造函数 ====================

    public RefreshTokenRequest() {
    }

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // ==================== Getter & Setter ====================

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
