package com.drp.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.auth.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 系统权限 Mapper 接口
 *
 * @author Nick
 */
@Mapper
public interface SysPermissionRepository extends BaseMapper<SysPermission> {

    /**
     * 根据权限编码查询权限
     *
     * @param code 权限编码
     * @return 权限信息
     */
    @Select("SELECT * FROM sys_permission WHERE code = #{code}")
    Optional<SysPermission> findByCode(@Param("code") String code);

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 1")
    List<SysPermission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询权限列表（直接授予的权限）
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_user_permission up ON p.id = up.permission_id " +
            "WHERE up.user_id = #{userId} AND p.status = 1")
    List<SysPermission> findByUserId(@Param("userId") Long userId);
}
