package com.drp.deploy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class DeployPreviewRequest {
    private Long projectId;
    private Long buildId;
    private String env;
    private String strategy;
    private List<Long> serverIds;
    private Boolean requireApproval;
    private String deployWindow;
    @Min(1)
    @Max(100)
    private Integer grayPercent;
    @Min(10)
    private Integer grayInterval;
    private String triggerType;
    private String changeTicket;
    private Boolean autoRollback;
}
