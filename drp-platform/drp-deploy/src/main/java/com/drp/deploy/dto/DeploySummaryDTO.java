package com.drp.deploy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DeploySummaryDTO {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long buildId;
    private Integer buildNumber;
    private String version;
    private String branch;
    private String env;
    private String strategy;
    private Integer status;
    private String statusDesc;
    private String approvalStatus;
    private String approvalStatusDesc;
    private String riskLevel;
    private String riskLevelDesc;
    private String triggerType;
    private String triggerTypeDesc;
    private String triggerUser;
    private String currentStep;
    private Integer duration;
    private List<String> serverNames = new ArrayList<>();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
