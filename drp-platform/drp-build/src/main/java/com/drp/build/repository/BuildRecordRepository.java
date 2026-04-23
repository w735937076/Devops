package com.drp.build.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.build.entity.BuildRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BuildRecordRepository extends BaseMapper<BuildRecord> {

    @Select("SELECT COALESCE(MAX(build_number), 0) FROM bld_build_record WHERE project_id = #{projectId}")
    int getMaxBuildNumber(@Param("projectId") Long projectId);
}
