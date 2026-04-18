package com.drp.auth.controller;

import com.drp.auth.dto.*;
import com.drp.auth.service.UserService;
import com.drp.common.dto.PageResponse;
import com.drp.common.result.Result;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器
 *
 * @author Nick
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<UserDTO> create(@Valid @RequestBody UserCreateRequest request) {
        log.info("创建用户 | username: {}", request.getUsername());
        UserDTO user = userService.create(request);
        return Result.success(user);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<UserDTO> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        log.info("更新用户 | id: {}", id);
        request.setId(id);
        UserDTO user = userService.update(request);
        return Result.success(user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除用户 | id: {}", id);
        userService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:view')")
    public Result<UserDTO> getById(@PathVariable Long id) {
        UserDTO user = userService.getById(id);
        return Result.success(user);
    }

    /**
     * 分页查询用户
     */
    @GetMapping
    @PreAuthorize("hasAuthority('system:user:view')")
    public Result<PageResponse<UserDTO>> queryPage(UserQueryRequest request) {
        PageResponse<UserDTO> page = userService.queryPage(request);
        return Result.success(page);
    }

    /**
     * 分配用户角色
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, java.util.Set<Long>> body) {
        log.info("分配角色 | userId: {} | roleIds: {}", id, body.get("roleIds"));
        userService.assignRoles(id, body.get("roleIds"));
        return Result.success("角色分配成功");
    }

    /**
     * 修改密码
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('system:user:password')")
    public Result<Void> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        log.info("修改密码 | userId: {}", id);
        userService.changePassword(id, body.get("oldPassword"), body.get("newPassword"));
        return Result.success("密码修改成功");
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('system:user:reset-password')")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        log.info("重置密码 | userId: {}", id);
        userService.resetPassword(id, body.get("newPassword"));
        return Result.success("密码重置成功");
    }
}