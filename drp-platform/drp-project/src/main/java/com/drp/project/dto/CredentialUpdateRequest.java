package com.drp.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * 更新凭证请求
 *
 * @author Nick
 */
public class CredentialUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "凭证名称不能为空")
    @Size(max = 100, message = "凭证名称最多100个字符")
    private String name;

    @NotBlank(message = "凭证类型不能为空")
    @Size(max = 20, message = "凭证类型最多20个字符")
    private String type;

    @Size(max = 100, message = "账号/标识最多100个字符")
    private String account;

    private String secretContent;

    @Size(max = 255, message = "描述最多255个字符")
    private String description;

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

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSecretContent() {
        return secretContent;
    }

    public void setSecretContent(String secretContent) {
        this.secretContent = secretContent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
