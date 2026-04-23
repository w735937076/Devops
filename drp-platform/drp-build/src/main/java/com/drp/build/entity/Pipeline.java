package com.drp.build.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bld_pipeline")
public class Pipeline {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private String name;
    private String description;

    /**
     * 阶段配置 (JSON 格式)
     * 结构: [{"name": "checkout", "type": "GIT_CLONE", "enabled": true}, ...]
     */
    private String stages;

    /**
     * 构建参数 (JSON 格式)
     * 结构: {"jdk": "17", "maven": "3.8.6", "node": "18.x"}
     */
    private String buildParams;

    private Integer triggerOnPush;
    private Integer triggerOnTag;
    private Integer triggerOnSchedule;
    private String cronExpression;

    private Integer timeout;
    private Integer status;
    private Integer isDefault;

    private Long createUser;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
