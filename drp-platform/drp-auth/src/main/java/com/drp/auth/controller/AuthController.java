package com.drp.auth.controller;

import com.drp.auth.dto.LoginRequest;
import com.drp.auth.dto.LoginResponse;
import com.drp.auth.dto.RefreshTokenRequest;
import com.drp.auth.dto.UserDTO;
import com.drp.auth.exception.AuthException;
import com.drp.auth.service.AuthService;
import com.drp.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 提供用户登录、登出、Token刷新、用户信息等接口
 *
 * @author Nick
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /**
     * 请求头中的客户端 IP 名称
     */
    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ==================== 公开接口 ====================

    /**
     * 用户登录
     * <p>
     * POST /api/auth/login
     *
     * @param request 登录请求
     * @return 登录响应 (包含 Access Token, Refresh Token, 用户信息)
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest) {
        // 获取客户端 IP
        String clientIp = getClientIp(httpRequest);

        log.info("登录请求 | username: {} | ip: {}", request.getUsername(), clientIp);

        LoginResponse response = authService.login(request, clientIp);

        log.info("登录成功 | username: {} | ip: {}", request.getUsername(), clientIp);

        return Result.success(response);
    }

    /**
     * 刷新 Token
     * <p>
     * POST /api/auth/refresh
     *
     * @param request 包含 refreshToken 的请求体
     * @return 新的登录响应
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("刷新 Token 请求");

        LoginResponse response = authService.refresh(request);

        log.debug("Token 刷新成功");

        return Result.success(response);
    }

    // ==================== 受保护接口 ====================

    /**
     * 获取当前用户信息
     * <p>
     * GET /api/auth/userinfo
     *
     * @return 当前登录用户信息
     */
    @GetMapping("/userinfo")
    public Result<UserDTO> getCurrentUserInfo() {
        // 从安全上下文中获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthException("用户未认证");
        }

        String username = authentication.getName();

        log.debug("获取用户信息 | username: {}", username);

        UserDTO userDTO = authService.getCurrentUser(username);

        return Result.success(userDTO);
    }

    /**
     * 用户登出
     * <p>
     * POST /api/auth/logout
     *
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            log.info("用户登出 | username: {}", username);
            authService.logout(username);
        }

        // 清除安全上下文
        SecurityContextHolder.clearContext();

        return Result.success("登出成功");
    }

    /**
     * 验证 Token 状态
     * <p>
     * GET /api/auth/validate
     *
     * @param token Access Token
     * @return Token 有效性
     */
    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        // 去掉 "Bearer " 前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        boolean isValid = authService.validateToken(token);

        return Result.success(isValid);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取客户端真实 IP
     * <p>
     * 优先从 X-Forwarded-For 头获取，若无则从 RemoteAddr 获取
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader(X_FORWARDED_FOR_HEADER);

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果是多个 IP (经过代理)，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
