package com.drp.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * 更新项目请求
 *
 * @author Nick
 */
public class ProjectUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称最多100个字符")
    private String name;

    @NotBlank(message = "项目类型不能为空")
    private String type;

    @Size(max = 500, message = "项目描述最多500个字符")
    private String description;

    @NotBlank(message = "Git仓库地址不能为空")
    @Size(max = 500, message = "Git仓库地址最多500个字符")
    private String gitUrl;

    private Long credentialId;

    private String defaultBranch;

    private String buildConfig;

    private Integer status;

    // ==================== Getter & Setter ====================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public Long getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(Long credentialId) {
        this.credentialId = credentialId;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public String getBuildConfig() {
        return buildConfig;
    }

    public void setBuildConfig(String buildConfig) {
        this.buildConfig = buildConfig;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}