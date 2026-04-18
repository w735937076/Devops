package com.drp.auth.service;

import com.drp.auth.dto.*;
import com.drp.common.dto.PageResponse;

import java.util.List;

/**
 * 权限管理服务接口
 *
 * @author Nick
 */
public interface PermissionService {

    /**
     * 创建权限
     */
    PermissionDTO create(PermissionCreateRequest request);

    /**
     * 更新权限
     */
    PermissionDTO update(PermissionUpdateRequest request);

    /**
     * 删除权限
     */
    void delete(Long id);

    /**
     * 根据ID获取权限
     */
    PermissionDTO getById(Long id);

    /**
     * 根据编码获取权限
     */
    PermissionDTO getByCode(String code);

    /**
     * 分页查询权限
     */
    PageResponse<PermissionDTO> queryPage(PermissionQueryRequest request);

    /**
     * 获取所有权限（树形结构）
     */
    List<PermissionDTO> listAll();

    /**
     * 获取所有权限（扁平列表）
     */
    List<PermissionDTO> listAllFlat();

    /**
     * 根据角色ID获取权限ID列表
     */
    List<Long> getPermissionIdsByRoleId(Long roleId);

    /**
     * 分配角色权限
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);
}