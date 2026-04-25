package com.drp.notify.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "ntf_notification_config", autoResultMap = true)
public class NotificationConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String channel;

    private String event;

    private Boolean enabled;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;

    private String recipients;

    private Long templateId;

    private Integer rateLimit;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
