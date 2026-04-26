package com.drp.deploy.controller;

import com.drp.common.dto.PageResponse;
import com.drp.common.result.Result;
import com.drp.deploy.dto.*;
import com.drp.deploy.service.DeployService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deploys")
@RequiredArgsConstructor
public class DeployController {

    private final DeployService deployService;

    @GetMapping
    public Result<PageResponse<DeploySummaryDTO>> queryPage(DeployQueryRequest request) {
        request.validate();
        return Result.success(deployService.queryPage(request));
    }

    @GetMapping("/pending-approvals")
    public Result<List<DeploySummaryDTO>> pendingApprovals() {
        return Result.success(deployService.getPendingApprovals());
    }

    @GetMapping("/{id}")
    public Result<DeployDetailDTO> getDetail(@PathVariable Long id) {
        return Result.success(deployService.getDetail(id));
    }

    @PostMapping("/preview")
    public Result<DeployPreviewDTO> preview(@RequestBody DeployPreviewRequest request,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(deployService.preview(request, getUsername(userDetails)));
    }

    @PostMapping
    public Result<DeployDetailDTO> create(@Valid @RequestBody DeployCreateRequest request,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success("部署任务已创建", deployService.create(request, getUsername(userDetails)));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails) {
        deployService.cancel(id, getUsername(userDetails));
        return Result.success("部署已取消");
    }

    @PostMapping("/{id}/retry")
    public Result<DeployDetailDTO> retry(@PathVariable Long id,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success("部署已重试", deployService.retry(id, getUsername(userDetails)));
    }

    @PostMapping("/{id}/rollback")
    public Result<DeployDetailDTO> rollback(@PathVariable Long id,
                                            @RequestBody(required = false) DeployRollbackRequest request,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        String reason = request == null ? null : request.getReason();
        return Result.success("回滚成功", deployService.rollback(id, getUsername(userDetails), reason));
    }

    @GetMapping("/{id}/log")
    public Result<String> getLog(@PathVariable Long id) {
        return Result.success(deployService.getLog(id));
    }

    @GetMapping("/{id}/health")
    public Result<List<DeployCheckItemDTO>> health(@PathVariable Long id) {
        return Result.success(deployService.getHealthChecks(id));
    }

    @PostMapping("/{id}/approval/approve")
    public Result<DeployDetailDTO> approve(@PathVariable Long id,
                                           @RequestBody(required = false) DeployApprovalActionRequest request,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        String comment = request == null ? null : request.getComment();
        return Result.success("审批通过", deployService.approve(id, getUsername(userDetails), comment));
    }

    @PostMapping("/{id}/approval/reject")
    public Result<DeployDetailDTO> reject(@PathVariable Long id,
                                          @RequestBody(required = false) DeployApprovalActionRequest request,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        String comment = request == null ? null : request.getComment();
        return Result.success("审批已驳回", deployService.reject(id, getUsername(userDetails), comment));
    }

    private String getUsername(UserDetails userDetails) {
        return userDetails == null ? "system" : userDetails.getUsername();
    }
}
