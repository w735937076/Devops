package com.drp.auth.service;

import com.drp.auth.dto.LoginRequest;
import com.drp.auth.dto.LoginResponse;
import com.drp.auth.dto.RefreshTokenRequest;
import com.drp.auth.dto.UserDTO;

/**
 * 认证服务接口
 *
 * @author Nick
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @param ip      登录IP
     * @return 登录响应 (包含Token和用户信息)
     */
    LoginResponse login(LoginRequest request, String ip);

    /**
     * 用户登出
     *
     * @param username 用户名
     */
    void logout(String username);

    /**
     * 刷新 Token
     *
     * @param request 刷新请求
     * @return 新的登录响应
     */
    LoginResponse refresh(RefreshTokenRequest request);

    /**
     * 获取当前用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserDTO getCurrentUser(String username);

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return true-有效
     */
    boolean validateToken(String token);
}
