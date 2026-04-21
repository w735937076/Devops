package com.drp.project.controller;

import com.drp.common.result.Result;
import com.drp.project.dto.BranchPolicyCreateRequest;
import com.drp.project.dto.BranchPolicyDTO;
import com.drp.project.dto.BranchPolicyUpdateRequest;
import com.drp.project.service.BranchPolicyService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分支策略控制器
 *
 * @author Nick
 */
@RestController
@RequestMapping("/api/projects/{projectId}/branch-policies")
public class BranchPolicyController {

    private static final Logger log = LoggerFactory.getLogger(BranchPolicyController.class);

    private final BranchPolicyService branchPolicyService;

    public BranchPolicyController(BranchPolicyService branchPolicyService) {
        this.branchPolicyService = branchPolicyService;
    }

    /**
     * 获取分支策略列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<List<BranchPolicyDTO>> list(@PathVariable("projectId") Long projectId) {
        return Result.success(branchPolicyService.listByProjectId(projectId));
    }

    /**
     * 创建分支策略
     */
    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<BranchPolicyDTO> create(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody BranchPolicyCreateRequest request) {
        request.setProjectId(projectId);
        log.info("创建分支策略 | projectId: {} | pattern: {}", projectId, request.getBranchPattern());
        return Result.success("创建成功", branchPolicyService.create(request));
    }

    /**
     * 更新分支策略
     */
    @PutMapping("/{policyId}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<BranchPolicyDTO> update(
            @PathVariable("projectId") Long projectId,
            @PathVariable("policyId") Long policyId,
            @Valid @RequestBody BranchPolicyUpdateRequest request) {
        log.info("更新分支策略 | id: {}", policyId);
        return Result.success("更新成功", branchPolicyService.update(policyId, request));
    }

    /**
     * 删除分支策略
     */
    @DeleteMapping("/{policyId}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<Void> delete(
            @PathVariable("projectId") Long projectId,
            @PathVariable("policyId") Long policyId) {
        log.info("删除分支策略 | id: {}", policyId);
        branchPolicyService.delete(policyId);
        return Result.success("删除成功");
    }
}