package com.drp.deploy.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeployPreviewDTO {
    private Long projectId;
    private String projectName;
    private Long buildId;
    private Integer buildNumber;
    private String version;
    private String branch;
    private String env;
    private String strategy;
    private String riskLevel;
    private String riskLevelDesc;
    private Boolean requireApproval;
    private Boolean windowValid;
    private String deployWindow;
    private Integer grayPercent;
    private Integer grayInterval;
    private Boolean hasRunningDeploy;
    private List<DeployServerDTO> servers = new ArrayList<>();
    private List<DeployCheckItemDTO> checks = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
}
