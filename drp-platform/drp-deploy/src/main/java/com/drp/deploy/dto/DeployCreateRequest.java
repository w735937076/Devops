package com.drp.deploy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DeployCreateRequest {
    @NotNull
    private Long projectId;
    @NotNull
    private Long buildId;
    @NotBlank
    private String env;
    @NotBlank
    private String strategy;
    private List<Long> serverIds;
    private Boolean requireApproval;
    private String deployWindow;
    private Integer grayPercent;
    private Integer grayInterval;
    private String triggerType;
    private String changeTicket;
    private Boolean autoRollback;
}
