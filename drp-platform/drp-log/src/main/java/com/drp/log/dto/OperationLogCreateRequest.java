package com.drp.log.dto;

import lombok.Data;

@Data
public class OperationLogCreateRequest {
    private String operator;
    private Long operatorId;
    private String operationType;
    private Long projectId;
    private String projectName;
    private String env;
    private String version;
    private String detail;
    private String ip;
    private String status;
    private Long duration;
    private String errorMessage;
}
