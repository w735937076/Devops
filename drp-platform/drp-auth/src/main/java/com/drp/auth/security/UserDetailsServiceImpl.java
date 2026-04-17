package com.drp.auth.security;

import com.drp.auth.entity.SysUser;
import com.drp.auth.repository.SysUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security UserDetailsService 实现类
 * <p>
 * 负责从数据库加载用户信息及其权限
 *
 * @author Nick
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final SysUserRepository sysUserRepository;

    public UserDetailsServiceImpl(SysUserRepository sysUserRepository) {
        this.sysUserRepository = sysUserRepository;
    }

    /**
     * 根据用户名加载用户详情
     *
     * @param username 用户名
     * @return UserDetails 用户详情
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("开始加载用户详情: {}", username);

        // 从数据库查询用户
        SysUser sysUser = sysUserRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("用户不存在: {}", username);
                    return new UsernameNotFoundException("用户不存在: " + username);
                });

        // 构建 Spring Security UserDetails
        return buildUserDetails(sysUser);
    }

    /**
     * 根据用户ID加载用户详情
     *
     * @param userId 用户ID
     * @return UserDetails 用户详情
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("开始加载用户详情, userId: {}", userId);

        SysUser sysUser = sysUserRepository.selectById(userId);
        if (sysUser == null) {
            log.warn("用户不存在, userId: {}", userId);
            throw new UsernameNotFoundException("用户不存在, userId: " + userId);
        }

        return buildUserDetails(sysUser);
    }

    /**
     * 构建 UserDetails 对象
     *
     * @param sysUser 系统用户实体
     * @return UserDetails 对象
     */
    private UserDetails buildUserDetails(SysUser sysUser) {
        // 获取用户权限列表
        Collection<? extends GrantedAuthority> authorities = buildAuthorities(sysUser);

        // 构建 UserDetails
        return new User(
                sysUser.getUsername(),
                sysUser.getPassword(),
                sysUser.isEnabled(),
                sysUser.isAccountNonExpired(),
                sysUser.isCredentialsNonExpired(),
                sysUser.isAccountNonLocked(),
                authorities
        );
    }

    /**
     * 构建用户权限列表
     *
     * @param sysUser 系统用户实体
     * @return 权限列表
     */
    private Collection<? extends GrantedAuthority> buildAuthorities(SysUser sysUser) {
        // 获取用户的角色列表
        List<String> roles = sysUser.getRoles().stream()
                .map(role -> "ROLE_" + role.getCode())  // 添加 ROLE_ 前缀
                .collect(Collectors.toList());

        // 获取用户的权限列表
        List<String> permissions = sysUser.getPermissions().stream()
                .map(perm -> perm.getCode())
                .collect(Collectors.toList());

        // 合并角色和权限
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        permissions.forEach(perm -> authorities.add(new SimpleGrantedAuthority(perm)));

        log.debug("用户 [{}] 权限列表: {}", sysUser.getUsername(), authorities);

        return authorities;
    }
}
