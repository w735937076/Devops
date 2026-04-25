package com.drp.log.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("log_operation")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String operator;

    private Long operatorId;

    private String operationType;

    private Long projectId;

    private String projectName;

    private String env;

    private String version;

    /**
     * 详细信息 (JSON 格式)
     */
    private String detail;

    private String ip;

    /**
     * 状态: SUCCESS, FAIL
     */
    private String status;

    /**
     * 耗时(毫秒)
     */
    private Long duration;

    /**
     * 错误信息
     */
    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
