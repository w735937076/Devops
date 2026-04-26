package com.drp.server.controller;

import com.drp.common.result.Result;
import com.drp.server.dto.ProjectDeployServerCreateRequest;
import com.drp.server.dto.ProjectDeployServerDTO;
import com.drp.server.dto.ProjectDeployServerUpdateRequest;
import com.drp.server.service.ProjectDeployServerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/deploy-servers")
@RequiredArgsConstructor
public class ProjectDeployServerController {

    private static final Logger log = LoggerFactory.getLogger(ProjectDeployServerController.class);

    private final ProjectDeployServerService projectDeployServerService;

    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<List<ProjectDeployServerDTO>> list(@PathVariable Long projectId,
                                                     @RequestParam(value = "env", required = false) String env) {
        return Result.success(projectDeployServerService.listByProjectId(projectId, env));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<ProjectDeployServerDTO> create(@PathVariable Long projectId,
                                                 @Valid @RequestBody ProjectDeployServerCreateRequest request) {
        request.setProjectId(projectId);
        log.info("创建项目部署服务器绑定 | projectId: {} | serverId: {} | env: {}", projectId, request.getServerId(), request.getEnv());
        return Result.success("创建成功", projectDeployServerService.create(request));
    }

    @PutMapping("/{bindingId}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<ProjectDeployServerDTO> update(@PathVariable Long projectId,
                                                 @PathVariable Long bindingId,
                                                 @Valid @RequestBody ProjectDeployServerUpdateRequest request) {
        log.info("更新项目部署服务器绑定 | projectId: {} | bindingId: {}", projectId, bindingId);
        return Result.success("更新成功", projectDeployServerService.update(projectId, bindingId, request));
    }

    @DeleteMapping("/{bindingId}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<Void> delete(@PathVariable Long projectId,
                               @PathVariable Long bindingId) {
        log.info("删除项目部署服务器绑定 | projectId: {} | bindingId: {}", projectId, bindingId);
        projectDeployServerService.delete(projectId, bindingId);
        return Result.success("删除成功");
    }
}
