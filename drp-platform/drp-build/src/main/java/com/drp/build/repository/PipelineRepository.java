package com.drp.build.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.build.entity.Pipeline;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PipelineRepository extends BaseMapper<Pipeline> {
}
