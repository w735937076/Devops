package com.drp.server.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.server.entity.ProjectServer;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectServerRepository extends BaseMapper<ProjectServer> {

    List<ProjectServer> findByProjectId(Long projectId);

    List<ProjectServer> findByServerId(Long serverId);
}
