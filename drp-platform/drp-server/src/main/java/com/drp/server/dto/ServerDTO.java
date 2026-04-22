package com.drp.server.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServerDTO {
    private Integer id;
    private String name;
    private String hostname;
    private Integer port;
    private String username;
    private String groups;
    private String workDir;
    private String backupDir;
    private Integer status;
    private String statusDesc;
    private String env;
    private String tags;
    private String remark;
    private LocalDateTime lastHeartbeat;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
