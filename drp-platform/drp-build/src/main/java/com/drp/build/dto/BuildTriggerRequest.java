package com.drp.build.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuildTriggerRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "分支不能为空")
    private String branch;

    private String commitId;

    private Long pipelineId;

    private String triggerType = "MANUAL";
}
