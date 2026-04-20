package com.drp.project.dto;

import com.drp.common.dto.PageRequest;

/**
 * 项目分页查询请求
 *
 * @author Nick
 */
public class ProjectQueryRequest extends PageRequest {

    private static final long serialVersionUID = 1L;

    private String keyword;

    private String type;

    private Integer status;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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