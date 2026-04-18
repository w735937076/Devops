package com.drp.auth.security;

import com.drp.auth.entity.SysPermission;
import com.drp.auth.entity.SysRole;
import com.drp.auth.entity.SysUser;
import com.drp.auth.repository.SysPermissionRepository;
import com.drp.auth.repository.SysRoleRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final SysRoleRepository sysRoleRepository;
    private final SysPermissionRepository sysPermissionRepository;

    public UserDetailsServiceImpl(SysUserRepository sysUserRepository,
                                  SysRoleRepository sysRoleRepository,
                                  SysPermissionRepository sysPermissionRepository) {
        this.sysUserRepository = sysUserRepository;
        this.sysRoleRepository = sysRoleRepository;
        this.sysPermissionRepository = sysPermissionRepository;
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

        // 加载用户的角色和权限
        loadUserRolesAndPermissions(sysUser);

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

        // 加载用户的角色和权限
        loadUserRolesAndPermissions(sysUser);

        return buildUserDetails(sysUser);
    }

    /**
     * 加载用户的角色和权限
     *
     * @param sysUser 系统用户实体
     */
    private void loadUserRolesAndPermissions(SysUser sysUser) {
        Long userId = sysUser.getId();

        // 1. 加载用户角色
        List<SysRole> roles = sysRoleRepository.findByUserId(userId);
        sysUser.setRoles(new HashSet<>(roles));

        // 2. 加载用户权限（直接授予的权限）
        List<SysPermission> directPermissions = sysPermissionRepository.findByUserId(userId);

        // 3. 加载用户权限（通过角色获得的权限）
        Set<SysPermission> allPermissions = new HashSet<>(directPermissions);
        for (SysRole role : roles) {
            List<SysPermission> rolePermissions = sysPermissionRepository.findByRoleId(role.getId());
            allPermissions.addAll(rolePermissions);
        }
        sysUser.setPermissions(allPermissions);

        log.debug("用户 [{}] 角色: {}, 直接权限: {}, 角色权限: {}",
                sysUser.getUsername(),
                roles.stream().map(SysRole::getCode).collect(Collectors.toList()),
                directPermissions.stream().map(SysPermission::getCode).collect(Collectors.toList()),
                allPermissions.stream().map(SysPermission::getCode).collect(Collectors.toList()));
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
        // 获取用户的角色列表 (添加 ROLE_ 前缀)
        List<String> roleAuthorities = sysUser.getRoles().stream()
                .map(role -> "ROLE_" + role.getCode())
                .collect(Collectors.toList());

        // 获取用户的权限列表
        List<String> permissionAuthorities = sysUser.getPermissions().stream()
                .map(SysPermission::getCode)
                .collect(Collectors.toList());

        // 合并角色和权限
        List<GrantedAuthority> authorities = new ArrayList<>();
        roleAuthorities.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        permissionAuthorities.forEach(perm -> authorities.add(new SimpleGrantedAuthority(perm)));

        log.debug("用户 [{}] 权限列表: {}", sysUser.getUsername(), authorities);

        return authorities;
    }
}
