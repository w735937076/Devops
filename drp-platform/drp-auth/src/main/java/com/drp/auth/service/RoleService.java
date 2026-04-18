package com.drp.auth.service;

import com.drp.auth.dto.*;
import com.drp.common.dto.PageResponse;

import java.util.List;

/**
 * 角色管理服务接口
 *
 * @author Nick
 */
public interface RoleService {

    /**
     * 创建角色
     */
    RoleDTO create(RoleCreateRequest request);

    /**
     * 更新角色
     */
    RoleDTO update(RoleUpdateRequest request);

    /**
     * 删除角色
     */
    void delete(Long id);

    /**
     * 根据ID获取角色
     */
    RoleDTO getById(Long id);

    /**
     * 根据编码获取角色
     */
    RoleDTO getByCode(String code);

    /**
     * 分页查询角色
     */
    PageResponse<RoleDTO> queryPage(RoleQueryRequest request);

    /**
     * 获取所有角色
     */
    List<RoleDTO> listAll();

    /**
     * 分配角色权限
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色权限ID列表
     */
    List<Long> getPermissionIds(Long roleId);
}