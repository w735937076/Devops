package com.drp.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ServerUpdateRequest {

    @NotBlank(message = "服务器名称不能为空")
    private String name;

    @NotBlank(message = "主机地址不能为空")
    private String hostname;

    private Integer port = 22;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    private String privateKey;

    private String privateKeyPassphrase;

    private String groups;

    private String env;

    private String tags;

    private String remark;

    private String workDir = "/opt/drp";

    private String backupDir = "/opt/drp/backup";
}
