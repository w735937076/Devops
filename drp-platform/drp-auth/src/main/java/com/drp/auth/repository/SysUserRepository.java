package com.drp.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 系统用户 Mapper 接口
 *
 * @author Nick
 */
@Mapper
public interface SysUserRepository extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    Optional<SysUser> findByUsername(@Param("username") String username);

    /**
     * 根据用户名查询用户（包含角色和权限）
     *
     * @param username 用户名
     * @return 用户信息（包含角色和权限）
     */
    @Select("SELECT u.* FROM sys_user u " +
            "WHERE u.username = #{username} AND u.deleted = 0")
    Optional<SysUser> findByUsernameWithRoles(@Param("username") String username);
}
