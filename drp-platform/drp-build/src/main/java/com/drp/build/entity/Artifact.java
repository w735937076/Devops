package com.drp.build.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bld_artifact")
public class Artifact {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long buildRecordId;
    private String name;
    private String path;
    private Long size;
    private String downloadUrl;
    private String checksum;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
