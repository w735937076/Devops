package com.drp.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ServerGroupUpdateRequest {

    @NotBlank(message = "分组名称不能为空")
    private String name;

    private String description;

    private Integer sort = 0;
}
