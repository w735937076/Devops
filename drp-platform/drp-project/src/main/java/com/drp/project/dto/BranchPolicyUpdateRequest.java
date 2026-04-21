package com.drp.project.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 分支策略更新请求
 *
 * @author Nick
 */
public class BranchPolicyUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "分支匹配模式不能为空")
    private String branchPattern;

    /**
     * 是否允许自动触发：0-否, 1-是
     */
    private Integer allowAutoDeploy;

    /**
     * 是否需要人工审批：0-否, 1-是
     */
    private Integer requireApproval;

    // ==================== Getter & Setter ====================

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