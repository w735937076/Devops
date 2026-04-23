package com.drp.build.dto;

import lombok.Data;

@Data
public class BuildLogDTO {
    private Long buildId;
    private String content;
    private boolean finished;
    private String status;
}
