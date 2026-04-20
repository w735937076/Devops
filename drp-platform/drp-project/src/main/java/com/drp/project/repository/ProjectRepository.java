package com.drp.project.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.project.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 项目仓储
 *
 * @author Nick
 */
@Mapper
public interface ProjectRepository extends BaseMapper<Project> {

    /**
     * 统计引用某凭证的项目数量
     */
    @Select("SELECT COUNT(1) FROM prj_project WHERE credential_id = #{credentialId} AND deleted = 0")
    long countByCredentialId(@Param("credentialId") Long credentialId);
}
