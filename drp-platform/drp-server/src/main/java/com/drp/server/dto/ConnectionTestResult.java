package com.drp.server.dto;

import lombok.Data;

@Data
public class ConnectionTestResult {
    private String hostname;
    private Integer port;
    private boolean success;
    private String message;
    private Long costMs;
}
