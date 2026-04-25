package com.drp.notify.dto;

import lombok.Data;

import java.util.Map;

@Data
public class NotificationConfigDTO {
    private Long id;
    private Long projectId;
    private String projectName;
    private String channel;
    private String event;
    private Boolean enabled;
    private Map<String, Object> config;
    private String recipients;
    private Long templateId;
    private Integer rateLimit;
    private String createTime;
}
