package com.drp.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drp.auth.dto.*;
import com.drp.auth.entity.SysPermission;
import com.drp.auth.entity.SysRolePermission;
import com.drp.auth.exception.AuthException;
import com.drp.auth.repository.SysPermissionRepository;
import com.drp.auth.repository.SysRolePermissionRepository;
import com.drp.auth.service.PermissionService;
import com.drp.common.constant.ResultCode;
import com.drp.common.dto.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限管理服务实现类
 *
 * @author Nick
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @Autowired
    private SysPermissionRepository sysPermissionRepository;

    @Autowired
    private SysRolePermissionRepository sysRolePermissionRepository;

    @Override
    @Transactional
    public PermissionDTO create(PermissionCreateRequest request) {
        log.info("创建权限 | code: {}", request.getCode());

        // 检查权限编码是否存在
        if (sysPermissionRepository.findByCode(request.getCode()).isPresent()) {
            throw new AuthException(ResultCode.PERMISSION_EXISTS, "权限编码已存在");
        }

        // 创建权限实体
        SysPermission permission = new SysPermission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permission.setParentId(request.getParentId());
        permission.setType(request.getType());
        permission.setSort(request.getSort());
        permission.setIcon(request.getIcon());
        permission.setPath(request.getPath());
        permission.setComponent(request.getComponent());
        permission.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        sysPermissionRepository.insert(permission);

        log.info("创建权限成功 | id: {} | code: {}", permission.getId(), permission.getCode());
        return buildPermissionDTO(permission);
    }

    @Override
    @Transactional
    public PermissionDTO update(PermissionUpdateRequest request) {
        log.info("更新权限 | id: {}", request.getId());

        SysPermission permission = sysPermissionRepository.selectById(request.getId());
        if (permission == null) {
            throw new AuthException(ResultCode.PERMISSION_NOT_FOUND, "权限不存在");
        }

        // 更新字段
        if (request.getName() != null) {
            permission.setName(request.getName());
        }
        if (request.getDescription() != null) {
            permission.setDescription(request.getDescription());
        }
        if (request.getParentId() != null) {
            permission.setParentId(request.getParentId());
        }
        if (request.getSort() != null) {
            permission.setSort(request.getSort());
        }
        if (request.getIcon() != null) {
            permission.setIcon(request.getIcon());
        }
        if (request.getPath() != null) {
            permission.setPath(request.getPath());
        }
        if (request.getComponent() != null) {
            permission.setComponent(request.getComponent());
        }
        if (request.getStatus() != null) {
            permission.setStatus(request.getStatus());
        }

        sysPermissionRepository.updateById(permission);

        log.info("更新权限成功 | id: {}", permission.getId());
        return buildPermissionDTO(permission);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("删除权限 | id: {}", id);

        SysPermission permission = sysPermissionRepository.selectById(id);
        if (permission == null) {
            throw new AuthException(ResultCode.PERMISSION_NOT_FOUND, "权限不存在");
        }

        // 检查是否有子权限
        List<SysPermission> children = sysPermissionRepository.selectList(
                new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getParentId, id));
        if (children != null && !children.isEmpty()) {
            throw new AuthException(ResultCode.PERMISSION_EXISTS, "该权限存在子权限，无法删除");
        }

        // 删除角色权限关联
        sysRolePermissionRepository.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getPermissionId, id));

        // 删除权限
        sysPermissionRepository.deleteById(id);

        log.info("删除权限成功 | id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionDTO getById(Long id) {
        SysPermission permission = sysPermissionRepository.selectById(id);
        if (permission == null) {
            throw new AuthException(ResultCode.PERMISSION_NOT_FOUND, "权限不存在");
        }
        return buildPermissionDTO(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionDTO getByCode(String code) {
        SysPermission permission = sysPermissionRepository.findByCode(code)
                .orElseThrow(() -> new AuthException(ResultCode.PERMISSION_NOT_FOUND, "权限不存在"));
        return buildPermissionDTO(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermissionDTO> queryPage(PermissionQueryRequest request) {
        request.validate();

        Page<SysPermission> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();

        if (request.getCode() != null && !request.getCode().isEmpty()) {
            wrapper.like(SysPermission::getCode, request.getCode());
        }
        if (request.getName() != null && !request.getName().isEmpty()) {
            wrapper.like(SysPermission::getName, request.getName());
        }
        if (request.getType() != null && !request.getType().isEmpty()) {
            wrapper.eq(SysPermission::getType, request.getType());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SysPermission::getStatus, request.getStatus());
        }

        wrapper.orderByAsc(SysPermission::getSort);

        Page<SysPermission> result = sysPermissionRepository.selectPage(page, wrapper);

        List<PermissionDTO> records = result.getRecords().stream()
                .map(this::buildPermissionDTO)
                .collect(Collectors.toList());

        return PageResponse.of(records, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> listAll() {
        List<SysPermission> allPermissions = sysPermissionRepository.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .orderByAsc(SysPermission::getSort));

        // 构建树形结构
        return buildPermissionTree(allPermissions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> listAllFlat() {
        List<SysPermission> permissions = sysPermissionRepository.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .orderByAsc(SysPermission::getSort));
        return permissions.stream()
                .map(this::buildPermissionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        return sysRolePermissionRepository.findPermissionIdsByRoleId(roleId);
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

    /**
     * 构建权限 DTO
     */
    private PermissionDTO buildPermissionDTO(SysPermission permission) {
        PermissionDTO dto = new PermissionDTO();
        dto.setId(permission.getId());
        dto.setCode(permission.getCode());
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        dto.setParentId(permission.getParentId());
        dto.setType(permission.getType());
        dto.setSort(permission.getSort());
        dto.setIcon(permission.getIcon());
        dto.setPath(permission.getPath());
        dto.setComponent(permission.getComponent());
        dto.setStatus(permission.getStatus());
        dto.setStatusDesc(permission.getStatus() != null && permission.getStatus() == 1 ? "启用" : "禁用");
        dto.setCreateTime(permission.getCreateTime());
        dto.setCreateBy(permission.getCreateBy() != null ? permission.getCreateBy().toString() : null);
        dto.setUpdateTime(permission.getUpdateTime());
        dto.setUpdateBy(permission.getUpdateBy() != null ? permission.getUpdateBy().toString() : null);

        // 获取父权限名称
        if (permission.getParentId() != null) {
            SysPermission parent = sysPermissionRepository.selectById(permission.getParentId());
            if (parent != null) {
                dto.setParentName(parent.getName());
            }
        }

        return dto;
    }

    /**
     * 构建权限树
     */
    private List<PermissionDTO> buildPermissionTree(List<SysPermission> allPermissions) {
        Map<Long, PermissionDTO> dtoMap = new HashMap<>();
        List<PermissionDTO> rootPermissions = new ArrayList<>();

        // 先转换为 DTO 并建立映射
        for (SysPermission permission : allPermissions) {
            PermissionDTO dto = buildPermissionDTO(permission);
            dto.setChildren(new ArrayList<>());
            dtoMap.put(dto.getId(), dto);
        }

        // 构建树形结构
        for (PermissionDTO dto : dtoMap.values()) {
            if (dto.getParentId() == null) {
                rootPermissions.add(dto);
            } else {
                PermissionDTO parent = dtoMap.get(dto.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                } else {
                    rootPermissions.add(dto);
                }
            }
        }

        return rootPermissions;
    }
}