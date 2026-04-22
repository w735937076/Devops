package com.drp.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("prj_project_server")
public class ProjectServer {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long projectId;

    private Long serverId;

    private String env;

    private String deployPath;
}
