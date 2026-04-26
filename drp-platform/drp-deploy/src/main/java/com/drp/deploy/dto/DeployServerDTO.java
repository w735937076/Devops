package com.drp.deploy.dto;

import lombok.Data;

@Data
public class DeployServerDTO {
    private Integer id;
    private String name;
    private String hostname;
    private String env;
    private String deployPath;
    private Integer status;
    private String statusDesc;
}
