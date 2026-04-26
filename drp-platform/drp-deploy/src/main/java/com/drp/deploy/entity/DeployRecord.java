package com.drp.deploy.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dpl_deploy_record")
public class DeployRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long buildId;
    private String env;
    private String strategy;
    private Integer status;
    private String approvalStatus;
    private String riskLevel;
    private String triggerType;
    private String triggerUser;
    private String version;
    private String branch;
    private String serverIds;
    private String serverSnapshot;
    private String deployWindow;
    private String grayConfig;
    private String changeTicket;
    private Boolean windowValid;
    private Boolean autoRollback;
    private String currentStep;
    private Integer duration;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    private String errorMessage;
    private Long rollbackFromDeployId;
    private String logContent;
    private String healthChecks;
    private String summary;
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
