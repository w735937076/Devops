package com.drp.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分支发布策略实体
 *
 * @author Nick
 */
@TableName("prj_branch_policy")
public class BranchPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 分支匹配正则(如: release/*)
     */
    private String branchPattern;

    /**
     * 是否允许自动触发
     */
    private Integer allowAutoDeploy;

    /**
     * 是否需要人工审批
     */
    private Integer requireApproval;

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

    public Integer getRequireApproval() {
        return requireApproval;
    }

    public void setRequireApproval(Integer requireApproval) {
        this.requireApproval = requireApproval;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}