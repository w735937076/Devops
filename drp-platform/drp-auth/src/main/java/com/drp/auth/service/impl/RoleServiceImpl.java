package com.drp.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drp.auth.dto.*;
import com.drp.auth.entity.SysPermission;
import com.drp.auth.entity.SysRole;
import com.drp.auth.entity.SysRolePermission;
import com.drp.auth.entity.SysUserRole;
import com.drp.auth.exception.AuthException;
import com.drp.auth.repository.SysPermissionRepository;
import com.drp.auth.repository.SysRolePermissionRepository;
import com.drp.auth.repository.SysRoleRepository;
import com.drp.auth.repository.SysUserRoleRepository;
import com.drp.auth.service.RoleService;
import com.drp.common.constant.ResultCode;
import com.drp.common.dto.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现类
 *
 * @author Nick
 */
@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    private SysRoleRepository sysRoleRepository;

    @Autowired
    private SysPermissionRepository sysPermissionRepository;

    @Autowired
    private SysRolePermissionRepository sysRolePermissionRepository;

    @Autowired
    private SysUserRoleRepository sysUserRoleRepository;

    @Override
    @Transactional
    public RoleDTO create(RoleCreateRequest request) {
        log.info("创建角色 | code: {}", request.getCode());

        // 检查角色编码是否存在
        if (sysRoleRepository.findByCode(request.getCode()).isPresent()) {
            throw new AuthException(ResultCode.ROLE_EXISTS, "角色编码已存在");
        }

        // 创建角色实体
        SysRole role = new SysRole();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setType(request.getType() != null ? request.getType() : "CUSTOM");
        role.setSort(request.getSort() != null ? request.getSort() : 0);
        role.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        sysRoleRepository.insert(role);

        log.info("创建角色成功 | id: {} | code: {}", role.getId(), role.getCode());
        return buildRoleDTO(role);
    }

    @Override
    @Transactional
    public RoleDTO update(RoleUpdateRequest request) {
        log.info("更新角色 | id: {}", request.getId());

        SysRole role = sysRoleRepository.selectById(request.getId());
        if (role == null) {
            throw new AuthException(ResultCode.ROLE_NOT_FOUND, "角色不存在");
        }

        // 更新字段
        if (request.getName() != null) {
            role.setName(request.getName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getSort() != null) {
            role.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            role.setStatus(request.getStatus());
        }

        sysRoleRepository.updateById(role);

        log.info("更新角色成功 | id: {}", role.getId());
        return buildRoleDTO(role);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("删除角色 | id: {}", id);

        SysRole role = sysRoleRepository.selectById(id);
        if (role == null) {
            throw new AuthException(ResultCode.ROLE_NOT_FOUND, "角色不存在");
        }

        // 检查是否有用户使用此角色
        List<SysUserRole> userRoles = sysUserRoleRepository.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        if (userRoles != null && !userRoles.isEmpty()) {
            throw new AuthException(ResultCode.ROLE_EXISTS, "该角色已被用户使用，无法删除");
        }

        // 删除角色（建议使用逻辑删除，这里先做物理删除）
        sysRoleRepository.deleteById(id);

        log.info("删除角色成功 | id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getById(Long id) {
        SysRole role = sysRoleRepository.selectById(id);
        if (role == null) {
            throw new AuthException(ResultCode.ROLE_NOT_FOUND, "角色不存在");
        }
        return buildRoleDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getByCode(String code) {
        SysRole role = sysRoleRepository.findByCode(code)
                .orElseThrow(() -> new AuthException(ResultCode.ROLE_NOT_FOUND, "角色不存在"));
        return buildRoleDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleDTO> queryPage(RoleQueryRequest request) {
        request.validate();

        Page<SysRole> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        if (request.getCode() != null && !request.getCode().isEmpty()) {
            wrapper.like(SysRole::getCode, request.getCode());
        }
        if (request.getName() != null && !request.getName().isEmpty()) {
            wrapper.like(SysRole::getName, request.getName());
        }
        if (request.getType() != null && !request.getType().isEmpty()) {
            wrapper.eq(SysRole::getType, request.getType());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SysRole::getStatus, request.getStatus());
        }

        // 排序
        if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
            if (request.isAscending()) {
                wrapper.orderByAsc(SysRole::getSort);
            } else {
                wrapper.orderByDesc(SysRole::getSort);
            }
        } else {
            wrapper.orderByAsc(SysRole::getSort);
        }

        Page<SysRole> result = sysRoleRepository.selectPage(page, wrapper);

        List<RoleDTO> records = result.getRecords().stream()
                .map(this::buildRoleDTO)
                .collect(Collectors.toList());

        return PageResponse.of(records, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> listAll() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, 1);
        wrapper.orderByAsc(SysRole::getSort);

        List<SysRole> roles = sysRoleRepository.selectList(wrapper);
        return roles.stream()
                .map(this::buildRoleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        log.info("分配权限 | roleId: {} | permissionIds: {}", roleId, permissionIds);

        // 删除现有权限关联
        sysRolePermissionRepository.deleteByRoleId(roleId);

        // 添加新权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rp = new SysRolePermission(roleId, permissionId);
                sysRolePermissionRepository.insert(rp);
            }
        }

        log.info("分配权限成功 | roleId: {}", roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getPermissionIds(Long roleId) {
        return sysRolePermissionRepository.findPermissionIdsByRoleId(roleId);
    }

    private RoleDTO buildRoleDTO(SysRole role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setType(role.getType());
        dto.setSort(role.getSort());
        dto.setStatus(role.getStatus());
        dto.setStatusDesc(role.getStatus() != null && role.getStatus() == 1 ? "启用" : "禁用");
        dto.setCreateTime(role.getCreateTime());
        dto.setCreateBy(role.getCreateBy() != null ? role.getCreateBy().toString() : null);
        dto.setUpdateTime(role.getUpdateTime());
        dto.setUpdateBy(role.getUpdateBy() != null ? role.getUpdateBy().toString() : null);
        return dto;
    }
}