package com.drp.auth.controller;

import com.drp.auth.dto.*;
import com.drp.auth.service.PermissionService;
import com.drp.common.dto.PageResponse;
import com.drp.common.result.Result;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 *
 * @author Nick
 */
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private static final Logger log = LoggerFactory.getLogger(PermissionController.class);

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 创建权限
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public Result<PermissionDTO> create(@Valid @RequestBody PermissionCreateRequest request) {
        log.info("创建权限 | code: {}", request.getCode());
        PermissionDTO permission = permissionService.create(request);
        return Result.success(permission);
    }

    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public Result<PermissionDTO> update(@PathVariable("id") Long id, @Valid @RequestBody PermissionUpdateRequest request) {
        log.info("更新权限 | id: {}", id);
        request.setId(id);
        PermissionDTO permission = permissionService.update(request);
        return Result.success(permission);
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public Result<Void> delete(@PathVariable("id") Long id) {
        log.info("删除权限 | id: {}", id);
        permissionService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 根据ID获取权限
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public Result<PermissionDTO> getById(@PathVariable("id") Long id) {
        PermissionDTO permission = permissionService.getById(id);
        return Result.success(permission);
    }

    /**
     * 根据编码获取权限
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public Result<PermissionDTO> getByCode(@PathVariable("code") String code) {
        PermissionDTO permission = permissionService.getByCode(code);
        return Result.success(permission);
    }

    /**
     * 分页查询权限
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public Result<PageResponse<PermissionDTO>> queryPage(PermissionQueryRequest request) {
        PageResponse<PermissionDTO> page = permissionService.queryPage(request);
        return Result.success(page);
    }

    /**
     * 获取所有权限（树形结构）
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public Result<List<PermissionDTO>> listTree() {
        List<PermissionDTO> permissions = permissionService.listAll();
        return Result.success(permissions);
    }

    /**
     * 获取所有权限（扁平列表）
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public Result<List<PermissionDTO>> listAll() {
        List<PermissionDTO> permissions = permissionService.listAllFlat();
        return Result.success(permissions);
    }

    /**
     * 根据角色ID获取权限ID列表
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public Result<List<Long>> getPermissionIdsByRoleId(@PathVariable("roleId") Long roleId) {
        List<Long> permissionIds = permissionService.getPermissionIdsByRoleId(roleId);
        return Result.success(permissionIds);
    }
}