package com.drp.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.auth.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 系统角色 Mapper 接口
 *
 * @author Nick
 */
@Mapper
public interface SysRoleRepository extends BaseMapper<SysRole> {

    /**
     * 根据角色编码查询角色
     *
     * @param code 角色编码
     * @return 角色信息
     */
    @Select("SELECT * FROM sys_role WHERE code = #{code}")
    Optional<SysRole> findByCode(@Param("code") String code);

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<SysRole> findByUserId(@Param("userId") Long userId);
}
