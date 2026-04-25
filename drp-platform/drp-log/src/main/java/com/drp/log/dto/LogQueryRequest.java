package com.drp.log.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LogQueryRequest {
    private Long operatorId;
    private Long projectId;
    private String operationType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String keyword;
    private int page = 1;
    private int size = 10;
}
