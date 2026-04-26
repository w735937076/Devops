package com.drp.deploy.service;

import com.drp.common.dto.PageResponse;
import com.drp.deploy.dto.*;

import java.util.List;

public interface DeployService {
    PageResponse<DeploySummaryDTO> queryPage(DeployQueryRequest request);
    DeployDetailDTO getDetail(Long id);
    DeployPreviewDTO preview(DeployPreviewRequest request, String operator);
    DeployDetailDTO create(DeployCreateRequest request, String operator);
    void cancel(Long id, String operator);
    DeployDetailDTO retry(Long id, String operator);
    DeployDetailDTO rollback(Long id, String operator, String reason);
    String getLog(Long id);
    List<DeployCheckItemDTO> getHealthChecks(Long id);
    List<DeploySummaryDTO> getPendingApprovals();
    DeployDetailDTO approve(Long id, String approver, String comment);
    DeployDetailDTO reject(Long id, String approver, String comment);
}
