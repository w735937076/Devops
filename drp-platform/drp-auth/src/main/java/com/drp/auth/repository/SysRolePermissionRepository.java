package com.drp.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.auth.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色权限关联 Mapper 接口
 *
 * @author Nick
 */
@Mapper
public interface SysRolePermissionRepository extends BaseMapper<SysRolePermission> {

    /**
     * 根据角色ID删除所有权限关联
     */
    @Select("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    void deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID查询权限ID列表
     */
    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> findPermissionIdsByRoleId(@Param("roleId") Long roleId);
}