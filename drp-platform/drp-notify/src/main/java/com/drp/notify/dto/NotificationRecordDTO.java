package com.drp.notify.dto;

import lombok.Data;

@Data
public class NotificationRecordDTO {
    private Long id;
    private Long configId;
    private String channel;
    private String event;
    private String recipient;
    private String title;
    private String content;
    private String status;
    private String statusText;
    private Integer retryCount;
    private Integer maxRetry;
    private String errorMessage;
    private String sendTime;
    private String sentTime;
    private String externalId;
}
