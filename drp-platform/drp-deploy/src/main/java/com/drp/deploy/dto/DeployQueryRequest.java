package com.drp.deploy.dto;

import com.drp.common.dto.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeployQueryRequest extends PageRequest {
    private Long projectId;
    private String env;
    private Integer status;
    private String approvalStatus;
    private String riskLevel;
    private String triggerType;
}
