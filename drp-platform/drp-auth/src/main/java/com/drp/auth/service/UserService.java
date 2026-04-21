package com.drp.auth.service;

import com.drp.auth.dto.*;
import com.drp.common.dto.PageResponse;
import com.drp.user.api.dto.SimpleUserDTO;

import java.util.List;

/**
 * 用户管理服务接口
 *
 * @author Nick
 */
public interface UserService {

    /**
     * 创建用户
     */
    UserDTO create(UserCreateRequest request);

    /**
     * 更新用户
     */
    UserDTO update(UserUpdateRequest request);

    /**
     * 删除用户
     */
    void delete(Long id);

    /**
     * 根据ID获取用户
     */
    UserDTO getById(Long id);

    /**
     * 分页查询用户
     */
    PageResponse<UserDTO> queryPage(UserQueryRequest request);

    /**
     * 分配用户角色
     */
    void assignRoles(Long userId, java.util.Set<Long> roleIds);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 获取所有用户（简化信息，用于下拉选择）
     */
    List<SimpleUserDTO> listAllSimple();
}