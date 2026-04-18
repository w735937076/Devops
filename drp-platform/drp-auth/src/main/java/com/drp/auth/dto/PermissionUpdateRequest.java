package com.drp.auth.dto;

import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * 权限更新请求 DTO
 *
 * @author Nick
 */
public class PermissionUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @Size(max = 100, message = "权限名称最多100个字符")
    private String name;

    private String description;

    private Long parentId;

    private Integer sort;

    private String icon;

    private String path;

    private String component;

    private Integer status;

    // ==================== Getter & Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}