package com.drp.build.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.build.entity.Artifact;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArtifactRepository extends BaseMapper<Artifact> {
}
