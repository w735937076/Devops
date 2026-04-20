package com.drp.project.controller;

import com.drp.common.dto.PageResponse;
import com.drp.common.result.Result;
import com.drp.project.dto.ProjectCreateRequest;
import com.drp.project.dto.ProjectDTO;
import com.drp.project.dto.ProjectQueryRequest;
import com.drp.project.dto.ProjectUpdateRequest;
import com.drp.project.service.ProjectService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 项目管理控制器
 *
 * @author Nick
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<PageResponse<ProjectDTO>> queryPage(ProjectQueryRequest request) {
        return Result.pageSuccess(projectService.queryPage(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<ProjectDTO> getById(@PathVariable("id") Long id) {
        return Result.success(projectService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<ProjectDTO> create(@Valid @RequestBody ProjectCreateRequest request) {
        log.info("创建项目 | name: {} | code: {} | type: {}", request.getName(), request.getCode(), request.getType());
        return Result.success("创建成功", projectService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<ProjectDTO> update(@PathVariable("id") Long id, @Valid @RequestBody ProjectUpdateRequest request) {
        log.info("更新项目 | id: {}", id);
        return Result.success("更新成功", projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<Void> delete(@PathVariable("id") Long id) {
        log.info("删除项目 | id: {}", id);
        projectService.delete(id);
        return Result.success("删除成功");
    }
}