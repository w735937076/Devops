package com.drp.project.controller;

import com.drp.common.result.Result;
import com.drp.project.dto.EnvVariableCreateRequest;
import com.drp.project.dto.EnvVariableDTO;
import com.drp.project.dto.EnvVariableUpdateRequest;
import com.drp.project.service.EnvVariableService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 环境变量控制器
 *
 * @author Nick
 */
@RestController
@RequestMapping("/api/projects/{projectId}/variables")
public class EnvVariableController {

    private static final Logger log = LoggerFactory.getLogger(EnvVariableController.class);

    private final EnvVariableService envVariableService;

    public EnvVariableController(EnvVariableService envVariableService) {
        this.envVariableService = envVariableService;
    }

    /**
     * 获取环境变量列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<List<EnvVariableDTO>> list(
            @PathVariable("projectId") Long projectId,
            @RequestParam(value = "env", required = false) String envCode) {
        return Result.success(envVariableService.listByProjectId(projectId, envCode));
    }

    /**
     * 创建环境变量
     */
    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<EnvVariableDTO> create(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody EnvVariableCreateRequest request) {
        request.setProjectId(projectId);
        log.info("创建环境变量 | projectId: {} | key: {} | env: {}", projectId, request.getVarKey(), request.getEnvCode());
        return Result.success("创建成功", envVariableService.create(request));
    }

    /**
     * 更新环境变量
     */
    @PutMapping("/{varId}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<EnvVariableDTO> update(
            @PathVariable("projectId") Long projectId,
            @PathVariable("varId") Long varId,
            @Valid @RequestBody EnvVariableUpdateRequest request) {
        log.info("更新环境变量 | id: {}", varId);
        return Result.success("更新成功", envVariableService.update(varId, request));
    }

    /**
     * 删除环境变量
     */
    @DeleteMapping("/{varId}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<Void> delete(
            @PathVariable("projectId") Long projectId,
            @PathVariable("varId") Long varId) {
        log.info("删除环境变量 | id: {}", varId);
        envVariableService.delete(varId);
        return Result.success("删除成功");
    }
}