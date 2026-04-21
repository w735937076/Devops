package com.drp.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drp.auth.dto.*;
import com.drp.auth.entity.SysRole;
import com.drp.user.api.dto.SimpleUserDTO;
import com.drp.auth.entity.SysUser;
import com.drp.auth.entity.SysUserRole;
import com.drp.auth.exception.AuthException;
import com.drp.auth.repository.SysRoleRepository;
import com.drp.auth.repository.SysUserRepository;
import com.drp.auth.repository.SysUserRoleRepository;
import com.drp.auth.service.UserService;
import com.drp.common.constant.ResultCode;
import com.drp.common.dto.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现类
 *
 * @author Nick
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private SysRoleRepository sysRoleRepository;

    @Autowired
    private SysUserRoleRepository sysUserRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO create(UserCreateRequest request) {
        log.info("创建用户 | username: {}", request.getUsername());

        // 检查用户名是否存在
        if (sysUserRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AuthException(ResultCode.USERNAME_EXISTS, "用户名已存在");
        }

        // 创建用户实体
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        user.setDeleted(false);

        sysUserRepository.insert(user);

        // 分配角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), request.getRoleIds());
        }

        log.info("创建用户成功 | id: {} | username: {}", user.getId(), user.getUsername());
        return buildUserDTO(user);
    }

    @Override
    @Transactional
    public UserDTO update(UserUpdateRequest request) {
        log.info("更新用户 | id: {}", request.getId());

        SysUser user = sysUserRepository.selectById(request.getId());
        if (user == null) {
            throw new AuthException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        // 更新字段
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        sysUserRepository.updateById(user);

        // 更新角色
        if (request.getRoleIds() != null) {
            assignRoles(user.getId(), request.getRoleIds());
        }

        log.info("更新用户成功 | id: {}", user.getId());
        return buildUserDTO(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("删除用户 | id: {}", id);

        SysUser user = sysUserRepository.selectById(id);
        if (user == null) {
            throw new AuthException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        // 逻辑删除
        user.setDeleted(true);
        sysUserRepository.updateById(user);

        // 删除用户角色关联
        sysUserRoleRepository.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));

        log.info("删除用户成功 | id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getById(Long id) {
        SysUser user = sysUserRepository.selectById(id);
        if (user == null) {
            throw new AuthException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        return buildUserDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> queryPage(UserQueryRequest request) {
        request.validate();

        Page<SysUser> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            wrapper.like(SysUser::getUsername, request.getUsername());
        }
        if (request.getRealName() != null && !request.getRealName().isEmpty()) {
            wrapper.like(SysUser::getRealName, request.getRealName());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            wrapper.like(SysUser::getEmail, request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            wrapper.like(SysUser::getPhone, request.getPhone());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, request.getStatus());
        }

        // 排除已删除
        wrapper.eq(SysUser::getDeleted, false);

        // 排序
        if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
            if (request.isAscending()) {
                wrapper.orderByAsc(SysUser::getId);
            } else {
                wrapper.orderByDesc(SysUser::getId);
            }
        } else {
            wrapper.orderByDesc(SysUser::getId);
        }

        Page<SysUser> result = sysUserRepository.selectPage(page, wrapper);

        List<UserDTO> records = result.getRecords().stream()
                .map(this::buildUserDTO)
                .collect(Collectors.toList());

        return PageResponse.of(records, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, Set<Long> roleIds) {
        log.info("分配角色 | userId: {} | roleIds: {}", userId, roleIds);

        // 删除现有角色关联
        sysUserRoleRepository.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));

        // 添加新角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                sysUserRoleRepository.insert(ur);
            }
        }

        log.info("分配角色成功 | userId: {}", userId);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改密码 | userId: {}", userId);

        SysUser user = sysUserRepository.selectById(userId);
        if (user == null) {
            throw new AuthException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AuthException(ResultCode.USERNAME_PASSWORD_ERROR, "原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserRepository.updateById(user);

        log.info("修改密码成功 | userId: {}", userId);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        log.info("重置密码 | userId: {}", userId);

        SysUser user = sysUserRepository.selectById(userId);
        if (user == null) {
            throw new AuthException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserRepository.updateById(user);

        log.info("重置密码成功 | userId: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimpleUserDTO> listAllSimple() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeleted, false);
        wrapper.orderByDesc(SysUser::getId);

        List<SysUser> users = sysUserRepository.selectList(wrapper);

        return users.stream()
                .map(user -> {
                    SimpleUserDTO dto = new SimpleUserDTO();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setRealName(user.getRealName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private UserDTO buildUserDTO(SysUser user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setStatus(user.getStatus());
        dto.setStatusDesc(user.isEnabled() ? "启用" : "禁用");
        dto.setCreateTime(user.getCreateTime());
        dto.setCreateBy(user.getCreateBy() != null ? user.getCreateBy().toString() : null);

        // 获取角色信息
        List<SysRole> roles = sysRoleRepository.findByUserId(user.getId());
        List<UserDTO.RoleDTO> roleDTOs = roles.stream()
                .map(role -> new UserDTO.RoleDTO(role.getId(), role.getCode(), role.getName()))
                .collect(Collectors.toList());
        dto.setRoles(roleDTOs);

        return dto;
    }
}