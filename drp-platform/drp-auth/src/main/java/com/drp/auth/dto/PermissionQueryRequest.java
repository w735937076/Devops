package com.drp.auth.dto;

import com.drp.common.dto.PageRequest;

import java.io.Serializable;

/**
 * 权限查询请求 DTO
 *
 * @author Nick
 */
public class PermissionQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;

    private String name;

    private String type;

    private Integer status;

    // ==================== Getter & Setter ====================

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}