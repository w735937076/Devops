package com.drp.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;

/**
 * 环境变量创建请求
 *
 * @author Nick
 */
public class EnvVariableCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目ID（由路径参数传入，无需验证）
     */
    private Long projectId;

    @NotBlank(message = "环境编码不能为空")
    @Pattern(regexp = "^(dev|test|prod)$", message = "环境编码只能是 dev、test、prod")
    private String envCode;

    @NotBlank(message = "变量Key不能为空")
    private String varKey;

    @NotBlank(message = "变量值不能为空")
    private String varValue;

    /**
     * 是否脱敏显示：0-否, 1-是
     */
    private Integer isSecret = 0;

    // ==================== Getter & Setter ====================

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getVarKey() {
        return varKey;
    }

    public void setVarKey(String varKey) {
        this.varKey = varKey;
    }

    public String getVarValue() {
        return varValue;
    }

    public void setVarValue(String varValue) {
        this.varValue = varValue;
    }

    public Integer getIsSecret() {
        return isSecret;
    }

    public void setIsSecret(Integer isSecret) {
        this.isSecret = isSecret;
    }
}