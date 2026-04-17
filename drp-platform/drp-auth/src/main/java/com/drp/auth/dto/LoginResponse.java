package com.drp.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录响应 DTO
 *
 * @author Nick
 */
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== Token 信息 ====================

    /**
     * Access Token (JWT)
     */
    private String accessToken;

    /**
     * Refresh Token
     */
    private String refreshToken;

    /**
     * Token 类型 (Bearer)
     */
    private String tokenType;

    /**
     * Access Token 过期时间 (秒)
     */
    private Long expiresIn;

    // ==================== 用户信息 ====================

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 权限列表
     */
    private List<String> permissions;

    // ==================== 登录信息 ====================

    /**
     * 登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime loginTime;

    // ==================== 构造函数 ====================

    public LoginResponse() {
    }

    /**
     * 全参数构造函数
     */
    public LoginResponse(String accessToken, String refreshToken, String tokenType, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    // ==================== Builder 模式 ====================

    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }

    public static class LoginResponseBuilder {
        private final LoginResponse response = new LoginResponse();

        public LoginResponseBuilder accessToken(String accessToken) {
            response.setAccessToken(accessToken);
            return this;
        }

        public LoginResponseBuilder refreshToken(String refreshToken) {
            response.setRefreshToken(refreshToken);
            return this;
        }

        public LoginResponseBuilder tokenType(String tokenType) {
            response.setTokenType(tokenType);
            return this;
        }

        public LoginResponseBuilder expiresIn(Long expiresIn) {
            response.setExpiresIn(expiresIn);
            return this;
        }

        public LoginResponseBuilder userId(Long userId) {
            response.setUserId(userId);
            return this;
        }

        public LoginResponseBuilder username(String username) {
            response.setUsername(username);
            return this;
        }

        public LoginResponseBuilder realName(String realName) {
            response.setRealName(realName);
            return this;
        }

        public LoginResponseBuilder email(String email) {
            response.setEmail(email);
            return this;
        }

        public LoginResponseBuilder phone(String phone) {
            response.setPhone(phone);
            return this;
        }

        public LoginResponseBuilder avatar(String avatar) {
            response.setAvatar(avatar);
            return this;
        }

        public LoginResponseBuilder roles(List<String> roles) {
            response.setRoles(roles);
            return this;
        }

        public LoginResponseBuilder permissions(List<String> permissions) {
            response.setPermissions(permissions);
            return this;
        }

        public LoginResponseBuilder loginTime(LocalDateTime loginTime) {
            response.setLoginTime(loginTime);
            return this;
        }

        public LoginResponse build() {
            return response;
        }
    }

    // ==================== Getter & Setter ====================

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
}
