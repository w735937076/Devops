package com.drp.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分支策略DTO
 *
 * @author Nick
 */
public class BranchPolicyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long projectId;
    private String branchPattern;
    private Integer allowAutoDeploy;
    private String allowAutoDeployDesc;
    private Integer requireApproval;
    private String requireApprovalDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    // ==================== Getter & Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getAllowAutoDeployDesc() {
        return allowAutoDeployDesc;
    }

    public void setAllowAutoDeployDesc(String allowAutoDeployDesc) {
        this.allowAutoDeployDesc = allowAutoDeployDesc;
    }

    public Integer getRequireApproval() {
        return requireApproval;
    }

    public void setRequireApproval(Integer requireApproval) {
        this.requireApproval = requireApproval;
    }

    public String getRequireApprovalDesc() {
        return requireApprovalDesc;
    }

    public void setRequireApprovalDesc(String requireApprovalDesc) {
        this.requireApprovalDesc = requireApprovalDesc;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}