package com.drp.deploy.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeployDetailDTO extends DeploySummaryDTO {
    private String deployWindow;
    private Boolean windowValid;
    private Boolean requireApproval;
    private Boolean autoRollback;
    private Integer grayPercent;
    private Integer grayInterval;
    private String changeTicket;
    private String errorMessage;
    private String summary;
    private String logContent;
    private Long rollbackFromDeployId;
    private List<DeployServerDTO> servers = new ArrayList<>();
    private List<DeployCheckItemDTO> healthChecks = new ArrayList<>();
    private List<DeployStepDTO> steps = new ArrayList<>();
    private List<ApprovalRecordDTO> approvalRecords = new ArrayList<>();
}
