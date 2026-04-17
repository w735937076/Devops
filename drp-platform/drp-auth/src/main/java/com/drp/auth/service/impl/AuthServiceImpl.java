package com.drp.auth.service.impl;

import com.drp.auth.dto.LoginRequest;
import com.drp.auth.dto.LoginResponse;
import com.drp.auth.dto.RefreshTokenRequest;
import com.drp.auth.dto.UserDTO;
import com.drp.auth.entity.SysPermission;
import com.drp.auth.entity.SysRole;
import com.drp.auth.entity.SysUser;
import com.drp.auth.exception.AuthException;
import com.drp.auth.repository.SysRoleRepository;
import com.drp.auth.repository.SysUserRepository;
import com.drp.auth.security.JwtTokenProvider;
import com.drp.auth.service.AuthService;
import com.drp.common.constant.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 *
 * @author Nick
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    /**
     * Token 存储前缀
     */
    private static final String TOKEN_PREFIX = "auth:token:";

    /**
     * RefreshToken 存储前缀
     */
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";

    /**
     * Access Token 有效期 (秒)
     */
    @Value("${jwt.expiration-ms:900000}")
    private long accessTokenExpirationMs;

    /**
     * Refresh Token 有效期 (秒)
     */
    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final SysUserRepository sysUserRepository;
    private final SysRoleRepository sysRoleRepository;
    private final StringRedisTemplate redisTemplate;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider,
                           SysUserRepository sysUserRepository,
                           SysRoleRepository sysRoleRepository,
                           StringRedisTemplate redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.sysUserRepository = sysUserRepository;
        this.sysRoleRepository = sysRoleRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @param ip      登录IP
     * @return 登录响应
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ip) {
        log.info("用户登录 | username: {} | ip: {}", request.getUsername(), ip);

        try {
            // 1. 执行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // 2. 设置安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. 获取用户详情
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 4. 加载用户完整信息 (包含角色和权限)
            SysUser sysUser = sysUserRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new AuthException(ResultCode.USER_NOT_FOUND, "用户不存在"));

            // 5. 加载用户角色
            List<SysRole> roles = sysRoleRepository.findByUserId(sysUser.getId());
            sysUser.setRoles(new java.util.HashSet<>(roles));

            // 6. 加载用户权限
            List<SysPermission> permissions = loadUserPermissions(sysUser.getId(), roles);
            sysUser.setPermissions(new java.util.HashSet<>(permissions));

            // 7. 生成 Token
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            // 8. 存储 Token 到 Redis
            storeTokens(sysUser.getUsername(), accessToken, refreshToken);

            // 9. 更新登录信息
            updateLoginInfo(sysUser.getId(), ip);

            // 10. 构建响应
            return buildLoginResponse(sysUser, accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            log.warn("登录失败 - 密码错误 | username: {}", request.getUsername());
            throw new AuthException(ResultCode.USERNAME_PASSWORD_ERROR, "用户名或密码错误");
        } catch (DisabledException e) {
            log.warn("登录失败 - 用户已禁用 | username: {}", request.getUsername());
            throw new AuthException(ResultCode.USER_DISABLED, "用户已被禁用");
        } catch (LockedException e) {
            log.warn("登录失败 - 用户已锁定 | username: {}", request.getUsername());
            throw new AuthException(ResultCode.USER_LOCKED, "用户已被锁定");
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("登录异常 | username: {} | error: {}", request.getUsername(), e.getMessage(), e);
            throw new AuthException(ResultCode.INTERNAL_ERROR, "登录失败，请稍后重试");
        }
    }

    /**
     * 用户登出
     *
     * @param username 用户名
     */
    @Override
    public void logout(String username) {
        log.info("用户登出 | username: {}", username);

        // 删除 Redis 中的 Token
        redisTemplate.delete(TOKEN_PREFIX + username);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + username);

        // 清除安全上下文
        SecurityContextHolder.clearContext();
    }

    /**
     * 刷新 Token
     *
     * @param request 刷新请求
     * @return 新的登录响应
     */
    @Override
    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        log.debug("开始刷新 Token");

        // 1. 验证 Refresh Token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("Refresh Token 无效");
            throw new AuthException(ResultCode.TOKEN_INVALID, "Refresh Token 无效");
        }

        // 2. 验证 Token 类型
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            log.warn("非法的 Token 类型");
            throw new AuthException(ResultCode.TOKEN_INVALID, "非法的 Token 类型");
        }

        // 3. 提取用户名
        String username = jwtTokenProvider.extractUsername(refreshToken);

        // 4. 检查 Redis 中的 Refresh Token 是否匹配
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + username);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            log.warn("Refresh Token 已失效");
            throw new AuthException(ResultCode.REFRESH_TOKEN_EXPIRED, "Refresh Token 已失效");
        }

        // 5. 加载用户信息
        SysUser sysUser = sysUserRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(ResultCode.USER_NOT_FOUND, "用户不存在"));

        // 6. 加载用户角色和权限
        List<SysRole> roles = sysRoleRepository.findByUserId(sysUser.getId());
        sysUser.setRoles(new java.util.HashSet<>(roles));
        List<SysPermission> permissions = loadUserPermissions(sysUser.getId(), roles);
        sysUser.setPermissions(new java.util.HashSet<>(permissions));

        // 7. 构建 UserDetails
        UserDetails userDetails = buildUserDetails(sysUser);

        // 8. 生成新 Token
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // 9. 更新 Redis 中的 Token
        storeTokens(username, newAccessToken, newRefreshToken);

        log.info("Token 刷新成功 | username: {}", username);

        return buildLoginResponse(sysUser, newAccessToken, newRefreshToken);
    }

    /**
     * 获取当前用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(String username) {
        SysUser sysUser = sysUserRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(ResultCode.USER_NOT_FOUND, "用户不存在"));

        return buildUserDTO(sysUser);
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return true-有效
     */
    @Override
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token) && jwtTokenProvider.isAccessToken(token);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 加载用户权限
     * <p>
     * 权限来源：用户直接授予的权限 + 用户角色关联的权限
     *
     * @param userId 用户ID
     * @param roles  用户角色列表
     * @return 权限列表
     */
    private List<SysPermission> loadUserPermissions(Long userId, List<SysRole> roles) {
        // 获取角色关联的权限
        return roles.stream()
                .flatMap(role -> {
                    List<SysPermission> perms = new java.util.ArrayList<>();
                    // 模拟从角色权限表中查询
                    return perms.stream();
                })
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 存储 Token 到 Redis
     *
     * @param username     用户名
     * @param accessToken  Access Token
     * @param refreshToken Refresh Token
     */
    private void storeTokens(String username, String accessToken, String refreshToken) {
        // 存储 Access Token
        redisTemplate.opsForValue().set(
                TOKEN_PREFIX + username,
                accessToken,
                accessTokenExpirationMs / 1000,
                TimeUnit.SECONDS
        );

        // 存储 Refresh Token
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + username,
                refreshToken,
                refreshTokenExpirationMs / 1000,
                TimeUnit.SECONDS
        );
    }

    /**
     * 更新登录信息
     *
     * @param userId 用户ID
     * @param ip     登录IP
     */
    private void updateLoginInfo(Long userId, String ip) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        sysUserRepository.updateById(user);
    }

    /**
     * 构建 UserDetails
     */
    private UserDetails buildUserDetails(SysUser sysUser) {
        List<String> roles = sysUser.getRoles().stream()
                .map(SysRole::getCode)
                .collect(Collectors.toList());

        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> (GrantedAuthority) () -> "ROLE_" + role)
                .collect(Collectors.toList());

        return new User(
                sysUser.getUsername(),
                sysUser.getPassword(),
                sysUser.isEnabled(),
                true, true, true,
                authorities
        );
    }

    /**
     * 构建登录响应
     */
    private LoginResponse buildLoginResponse(SysUser sysUser, String accessToken, String refreshToken) {
        // 提取角色和权限
        List<String> roles = sysUser.getRoles().stream()
                .map(SysRole::getCode)
                .collect(Collectors.toList());

        List<String> permissions = sysUser.getPermissions().stream()
                .map(SysPermission::getCode)
                .collect(Collectors.toList());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationMs / 1000)
                .userId(sysUser.getId())
                .username(sysUser.getUsername())
                .realName(sysUser.getRealName())
                .email(sysUser.getEmail())
                .phone(sysUser.getPhone())
                .avatar(sysUser.getAvatar())
                .roles(roles)
                .permissions(permissions)
                .loginTime(LocalDateTime.now())
                .build();
    }

    /**
     * 构建用户 DTO
     */
    private UserDTO buildUserDTO(SysUser sysUser) {
        UserDTO dto = new UserDTO();
        dto.setId(sysUser.getId());
        dto.setUsername(sysUser.getUsername());
        dto.setRealName(sysUser.getRealName());
        dto.setEmail(sysUser.getEmail());
        dto.setPhone(sysUser.getPhone());
        dto.setAvatar(sysUser.getAvatar());
        dto.setStatus(sysUser.getStatus());
        dto.setStatusDesc(sysUser.isEnabled() ? "启用" : "禁用");
        dto.setLastLoginTime(sysUser.getLastLoginTime());
        dto.setLastLoginIp(sysUser.getLastLoginIp());
        dto.setCreateTime(sysUser.getCreateTime());

        // 设置角色信息
        List<UserDTO.RoleDTO> roleDTOs = sysUser.getRoles().stream()
                .map(role -> new UserDTO.RoleDTO(role.getId(), role.getCode(), role.getName()))
                .collect(Collectors.toList());
        dto.setRoles(roleDTOs);

        // 设置权限信息
        List<String> permissions = sysUser.getPermissions().stream()
                .map(SysPermission::getCode)
                .collect(Collectors.toList());
        dto.setPermissions(permissions);

        return dto;
    }
}
