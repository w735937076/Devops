package com.drp.build.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bld_build_record")
public class BuildRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer buildNumber;
    private Long projectId;
    private Long pipelineId;

    private String branch;
    private String commitId;
    private String commitMessage;

    private String buildConfig;

    @TableField("status")
    private Integer status; // 0-排队, 1-运行中, 2-成功, 3-失败, 4-取消, 5-超时

    private String triggerType;
    private String triggerUser;

    private Integer duration;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    private String errorMessage;

    /**
     * 构建产物列表 (JSON 数组格式)
     * 结构: [{"name": "app.jar", "path": "/data/builds/1/app.jar", "size": 123456}]
     */
    private String artifacts;

    private String executorServer;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
