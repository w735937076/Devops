package com.drp.log.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OperationLogDTO {
    private Long id;
    private String operator;
    private Long operatorId;
    private String operationType;
    private String operationTypeText;
    private Long projectId;
    private String projectName;
    private String env;
    private String version;
    private String detail;
    private String ip;
    private String status;
    private String statusText;
    private Long duration;
    private String errorMessage;
    private LocalDateTime createTime;

    public String getStatusText() {
        return "SUCCESS".equals(status) ? "成功" : "失败";
    }

    public String getOperationTypeText() {
        if (operationType == null) return "";
        return switch (operationType) {
            case "BUILD" -> "构建";
            case "DEPLOY" -> "部署";
            case "ROLLBACK" -> "回滚";
            case "CREATE" -> "创建";
            case "UPDATE" -> "更新";
            case "DELETE" -> "删除";
            default -> operationType;
        };
    }
}
