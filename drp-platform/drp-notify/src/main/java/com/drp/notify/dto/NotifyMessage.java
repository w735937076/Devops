package com.drp.notify.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotifyMessage {
    private String title;
    private String content;
    private String projectName;
    private String env;
    private String version;
    private String operator;
    private String recipient;
    private String severity;
    private String externalId;
}
