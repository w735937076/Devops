package com.drp.notify.dto;

import lombok.Data;

@Data
public class NotificationTemplateDTO {
    private Long id;
    private String name;
    private String channel;
    private String event;
    private String titleTemplate;
    private String contentTemplate;
    private String variables;
    private Boolean enabled;
}
