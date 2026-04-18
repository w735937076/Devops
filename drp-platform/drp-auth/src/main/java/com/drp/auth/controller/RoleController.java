package com.drp.auth.controller;

import com.drp.auth.dto.*;
import com.drp.auth.service.RoleService;
import com.drp.common.dto.PageResponse;
import com.drp.common.result.Result;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 *
 * @author Nick
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private static final Logger log = LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 创建角色
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public Result<RoleDTO> create(@Valid @RequestBody RoleCreateRequest request) {
        log.info("创建角色 | code: {}", request.getCode());
        RoleDTO role = roleService.create(request);
        return Result.success(role);
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<RoleDTO> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        log.info("更新角色 | id: {}", id);
        request.setId(id);
        RoleDTO role = roleService.update(request);
        return Result.success(role);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除角色 | id: {}", id);
        roleService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 根据ID获取角色
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:view')")
    public Result<RoleDTO> getById(@PathVariable Long id) {
        RoleDTO role = roleService.getById(id);
        return Result.success(role);
    }

    /**
     * 根据编码获取角色
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('system:role:view')")
    public Result<RoleDTO> getByCode(@PathVariable String code) {
        RoleDTO role = roleService.getByCode(code);
        return Result.success(role);
    }

    /**
     * 分页查询角色
     */
    @GetMapping
    @PreAuthorize("hasAuthority('system:role:view')")
    public Result<PageResponse<RoleDTO>> queryPage(RoleQueryRequest request) {
        PageResponse<RoleDTO> page = roleService.queryPage(request);
        return Result.success(page);
    }

    /**
     * 获取所有角色
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('system:role:view')")
    public Result<List<RoleDTO>> listAll() {
        List<RoleDTO> roles = roleService.listAll();
        return Result.success(roles);
    }

    /**
     * 分配角色权限
     */
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        log.info("分配权限 | roleId: {} | permissionIds: {}", id, body.get("permissionIds"));
        roleService.assignPermissions(id, body.get("permissionIds"));
        return Result.success("权限分配成功");
    }

    /**
     * 获取角色权限ID列表
     */
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('system:role:view')")
    public Result<List<Long>> getPermissionIds(@PathVariable Long id) {
        List<Long> permissionIds = roleService.getPermissionIds(id);
        return Result.success(permissionIds);
    }
}