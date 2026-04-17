package com.drp.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>
 * 所有实体类都应该继承此类，以获得：
 * - 统一的ID主键
 * - 审计字段（创建时间、创建人、更新时间、更新人）
 * - 公共字段
 *
 * @author Nick
 */
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 主键 ====================

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    // ==================== 审计字段 ====================

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建人名称（冗余字段，避免关联查询）
     */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新人名称（冗余字段）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    // ==================== 公共字段 ====================

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 租户ID（用于多租户场景）
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Boolean deleted;

    // ==================== Getter & Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public String getCreateName() {
        return createName;
    }

    public void setCreateName(String createName) {
        this.createName = createName;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public String getUpdateName() {
        return updateName;
    }

    public void setUpdateName(String updateName) {
        this.updateName = updateName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
