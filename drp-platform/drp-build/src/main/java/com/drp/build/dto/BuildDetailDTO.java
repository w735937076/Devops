package com.drp.build.dto;

import com.drp.build.entity.BuildRecord;
import com.drp.build.entity.Pipeline;
import com.drp.build.enums.BuildStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /** 版本号 (格式: v{date}-{commitId}-{buildNumber}) */
    private String version;

    /** 流水线阶段列表 */
    private List<StageDetail> stages;

    /** 构建参数 */
    private Map<String, String> buildParams;

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

        // 生成版本号
        dto.setVersion(generateVersion(record));

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

    /**
     * 设置流水线信息和阶段
     */
    public void setPipelineInfo(Pipeline pipeline) {
        if (pipeline == null) return;
        setPipelineName(pipeline.getName());
        setStages(parseStages(pipeline.getStages()));
        setBuildParams(parseBuildParams(pipeline.getBuildParams()));
    }

    /**
     * 从构建配置设置构建参数
     */
    public void setBuildParamsFromConfig(String buildConfig) {
        if (buildConfig == null || buildConfig.isEmpty()) return;
        try {
            Map<String, String> params = objectMapper.readValue(buildConfig, new TypeReference<Map<String, String>>() {});
            if (getBuildParams() == null) {
                setBuildParams(params);
            } else {
                getBuildParams().putAll(params);
            }
        } catch (Exception e) {
            // ignore parse error
        }
    }

    /**
     * 生成版本号: v{date}-{commitId(前7位)}-{buildNumber}
     */
    private static String generateVersion(BuildRecord record) {
        String date = record.getCreateTime() != null
            ? record.getCreateTime().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String commit = record.getCommitId() != null && record.getCommitId().length() >= 7
            ? record.getCommitId().substring(0, 7)
            : (record.getCommitId() != null ? record.getCommitId() : "HEAD");
        return String.format("v%s-%s-%03d", date, commit, record.getBuildNumber());
    }

    /**
     * 解析流水线阶段
     */
    private List<StageDetail> parseStages(String stagesJson) {
        List<StageDetail> result = new ArrayList<>();
        if (stagesJson == null || stagesJson.isEmpty()) {
            return result;
        }
        try {
            List<Map<String, Object>> stageList = objectMapper.readValue(
                stagesJson, new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> stage : stageList) {
                StageDetail detail = new StageDetail();
                detail.setName((String) stage.get("name"));
                detail.setType((String) stage.get("type"));
                detail.setEnabled(stage.get("enabled") != null ? (Boolean) stage.get("enabled") : true);
                result.add(detail);
            }
        } catch (Exception e) {
            // ignore parse error
        }
        return result;
    }

    /**
     * 解析构建参数
     */
    private Map<String, String> parseBuildParams(String buildParamsJson) {
        if (buildParamsJson == null || buildParamsJson.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(buildParamsJson, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    @Data
    public static class StageDetail {
        private String name;
        private String type;
        private Boolean enabled;
        private Integer duration;
        private String status; // PENDING, RUNNING, SUCCESS, FAIL, SKIP
    }
}
