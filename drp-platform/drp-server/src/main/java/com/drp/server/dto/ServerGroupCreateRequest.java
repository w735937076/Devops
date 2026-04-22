package com.drp.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ServerGroupCreateRequest {

    @NotBlank(message = "分组名称不能为空")
    private String name;

    @NotBlank(message = "分组编码不能为空")
    private String code;

    private String description;

    private Integer sort = 0;
}
