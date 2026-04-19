package com.drp.project.dto;

import com.drp.common.dto.PageRequest;

import java.io.Serializable;

/**
 * 凭证分页查询请求
 *
 * @author Nick
 */
public class CredentialQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyword;

    private String type;

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
}
