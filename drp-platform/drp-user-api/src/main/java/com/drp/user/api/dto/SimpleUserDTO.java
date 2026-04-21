package com.drp.user.api.dto;

import java.io.Serializable;

/**
 * 用户信息 DTO（简化版，用于跨服务调用）
 */
public class SimpleUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String realName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}