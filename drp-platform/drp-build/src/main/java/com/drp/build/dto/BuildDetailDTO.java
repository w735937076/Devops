package com.drp.build.dto;

import com.drp.build.entity.BuildRecord;
import com.drp.build.enums.BuildStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class BuildDetailDTO {

    private Long id;
    private Integer buildNumber;
    private Long projectId;
    private String projectName;
    private Long pipelineId;
    private String pipelineName;

    private String branch;
    private String commitId;
    private String commitMessage;

    private Integer status;
    private String statusDesc;

    private String triggerType;
    private String triggerUser;

    private Integer duration;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    private String errorMessage;
    private List<ArtifactDTO> artifacts;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static BuildDetailDTO fromEntity(BuildRecord record) {
        BuildDetailDTO dto = new BuildDetailDTO();
        dto.setId(record.getId());
        dto.setBuildNumber(record.getBuildNumber());
        dto.setProjectId(record.getProjectId());
        dto.setBranch(record.getBranch());
        dto.setCommitId(record.getCommitId());
        dto.setCommitMessage(record.getCommitMessage());
        dto.setStatus(record.getStatus());
        dto.setStatusDesc(BuildStatus.fromCode(record.getStatus()).getDescription());
        dto.setTriggerType(record.getTriggerType());
        dto.setTriggerUser(record.getTriggerUser());
        dto.setDuration(record.getDuration());
        dto.setStartTime(record.getStartTime());
        dto.setEndTime(record.getEndTime());
        dto.setErrorMessage(record.getErrorMessage());
        dto.setCreateTime(record.getCreateTime());

        // 解析构建产物
        if (record.getArtifacts() != null && !record.getArtifacts().isEmpty()) {
            try {
                List<ArtifactDTO> artifacts = objectMapper.readValue(
                    record.getArtifacts(),
                    new TypeReference<List<ArtifactDTO>>() {}
                );
                dto.setArtifacts(artifacts);
            } catch (Exception e) {
                dto.setArtifacts(new ArrayList<>());
            }
        } else {
            dto.setArtifacts(new ArrayList<>());
        }

        return dto;
    }
}
