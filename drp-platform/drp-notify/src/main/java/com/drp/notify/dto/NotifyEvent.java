package com.drp.notify.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class NotifyEvent {
    private String event;
    private String category;
    private String severity;
    private Long projectId;
    private String projectName;
    private String env;
    private String version;
    private String operator;
    private String operatorIp;
    private Map<String, Object> extraData;
    private LocalDateTime timestamp;
    private String title;
    private String content;
}
