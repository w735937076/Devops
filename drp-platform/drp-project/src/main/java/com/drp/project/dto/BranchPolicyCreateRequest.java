package com.drp.project.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 分支策略创建请求
 *
 * @author Nick
 */
public class BranchPolicyCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目ID（由路径参数传入，无需验证）
     */
    private Long projectId;

    @NotBlank(message = "分支匹配模式不能为空")
    private String branchPattern;

    /**
     * 是否允许自动触发：0-否, 1-是
     */
    private Integer allowAutoDeploy = 0;

    /**
     * 是否需要人工审批：0-否, 1-是
     */
    private Integer requireApproval = 0;

    // ==================== Getter & Setter ====================

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getBranchPattern() {
        return branchPattern;
    }

    public void setBranchPattern(String branchPattern) {
        this.branchPattern = branchPattern;
    }

    public Integer getAllowAutoDeploy() {
        return allowAutoDeploy;
    }

    public void setAllowAutoDeploy(Integer allowAutoDeploy) {
        this.allowAutoDeploy = allowAutoDeploy;
    }

    public Integer getRequireApproval() {
        return requireApproval;
    }

    public void setRequireApproval(Integer requireApproval) {
        this.requireApproval = requireApproval;
    }
}