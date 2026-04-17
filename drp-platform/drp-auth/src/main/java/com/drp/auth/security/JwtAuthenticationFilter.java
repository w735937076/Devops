package com.drp.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * <p>
 * 拦截每个请求，从请求头中提取 JWT Token 并进行认证
 *
 * @author Nick
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * Authorization 请求头前缀
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Authorization 请求头名称
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsServiceImpl userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * 执行过滤逻辑
     *
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求中提取 JWT Token
            String jwt = extractJwtFromRequest(request);

            // 验证 Token 并设置认证信息
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 验证 Token 类型
                if (!jwtTokenProvider.isAccessToken(jwt)) {
                    log.warn("非法的 Token 类型，仅支持 Access Token");
                } else {
                    // 从 Token 中提取用户名
                    String username = jwtTokenProvider.extractUsername(jwt);

                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 创建认证令牌
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());

                    // 设置认证详情
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 将认证信息存入安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("用户 [{}] 认证成功", username);
                }
            }
        } catch (Exception ex) {
            // 认证失败不影响请求继续执行，由 Security Config 的 Access Denied Handler 处理
            log.warn("JWT 认证失败: {}", ex.getMessage());
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 JWT Token
     *
     * @param request HTTP 请求
     * @return JWT Token 字符串，如果不存在则返回 null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        // 检查是否存在 Bearer Token
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            // 提取 Token 部分 (去掉 "Bearer " 前缀)
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * 判断是否为无需认证的路径
     * <p>
     * 此方法在 {@link #shouldNotFilter(HttpServletRequest)} 中被调用，
     * 用于判断哪些路径不需要进行 JWT 认证过滤
     *
     * @param request HTTP 请求
     * @return true-不需要过滤
     */
    public static boolean isPublicPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/refresh") ||
               path.startsWith("/actuator");
    }
}
