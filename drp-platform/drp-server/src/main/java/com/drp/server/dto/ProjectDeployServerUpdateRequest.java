package com.drp.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProjectDeployServerUpdateRequest {

    @NotNull(message = "服务器不能为空")
    private Long serverId;

    @NotBlank(message = "部署环境不能为空")
    private String env;

    @NotBlank(message = "部署路径不能为空")
    private String deployPath;

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getDeployPath() {
        return deployPath;
    }

    public void setDeployPath(String deployPath) {
        this.deployPath = deployPath;
    }
}
