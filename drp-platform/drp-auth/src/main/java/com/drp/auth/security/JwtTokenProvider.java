package com.drp.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT Token 提供者
 * <p>
 * 负责 JWT Token 的生成、验证、解析等操作
 *
 * @author Nick
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    /**
     * JWT 密钥 (Base64编码的256位密钥)
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Access Token 过期时间 (毫秒) - 默认15分钟
     */
    @Value("${jwt.expiration-ms:900000}")
    private long accessTokenExpirationMs;

    /**
     * Refresh Token 过期时间 (毫秒) - 默认7天
     */
    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    // ==================== Token 生成 ====================

    /**
     * 生成 Access Token
     *
     * @param authentication 认证信息
     * @return JWT Token 字符串
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateAccessToken(userDetails);
    }

    /**
     * 生成 Access Token (重载方法)
     *
     * @param userDetails 用户详情
     * @return JWT Token 字符串
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userDetails.getUsername());
        claims.put("roles", extractRoles(userDetails));
        claims.put("type", "access");
        return createToken(claims, userDetails.getUsername(), accessTokenExpirationMs);
    }

    /**
     * 生成 Refresh Token
     * <p>
     * Refresh Token 只包含最基本的用户标识，不包含权限信息
     *
     * @param userDetails 用户详情
     * @return Refresh Token 字符串
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshTokenExpirationMs);
    }

    /**
     * 创建 Token
     *
     * @param claims     声明信息
     * @param subject   主题 (用户名)
     * @param expiration 过期时间 (毫秒)
     * @return JWT Token 字符串
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // ==================== Token 解析 ====================

    /**
     * 从 Token 中提取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从 Token 中提取过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从 Token 中提取指定声明
     *
     * @param token          JWT Token
     * @param claimsResolver 声明解析函数
     * @param <T>            返回类型
     * @return 声明值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 提取所有声明
     *
     * @param token JWT Token
     * @return Claims 对象
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ==================== Token 验证 ====================

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return true-有效, false-无效
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (MalformedJwtException e) {
            log.warn("JWT Token 格式错误: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token 已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT Token 不支持: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT Token 参数错误: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("JWT Token 验证失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 验证 Token 类型是否为 Access Token
     *
     * @param token JWT Token
     * @return true-是 Access Token
     */
    public boolean isAccessToken(String token) {
        try {
            String type = extractClaim(token, claims -> claims.get("type", String.class));
            return "access".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证 Token 类型是否为 Refresh Token
     *
     * @param token JWT Token
     * @return true-是 Refresh Token
     */
    public boolean isRefreshToken(String token) {
        try {
            String type = extractClaim(token, claims -> claims.get("type", String.class));
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断 Token 是否已过期
     *
     * @param token JWT Token
     * @return true-已过期
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ==================== 辅助方法 ====================

    /**
     * 从 UserDetails 中提取角色列表
     *
     * @param userDetails 用户详情
     * @return 角色列表
     */
    private List<String> extractRoles(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    /**
     * 获取签名密钥
     *
     * @return SecretKey 对象
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 获取 Access Token 剩余有效时间 (秒)
     *
     * @param token JWT Token
     * @return 剩余秒数
     */
    public long getRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0;
        }
    }
}
