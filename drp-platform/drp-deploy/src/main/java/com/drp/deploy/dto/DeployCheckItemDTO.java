package com.drp.deploy.dto;

import lombok.Data;

@Data
public class DeployCheckItemDTO {
    private String key;
    private String name;
    private String status;
    private String message;
}
