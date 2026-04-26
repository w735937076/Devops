package com.drp.deploy.dto;

import lombok.Data;

@Data
public class DeployStepDTO {
    private String stepKey;
    private String stepName;
    private String status;
    private Integer duration;
    private String detail;
}
