package com.drp.project.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.project.entity.ProjectMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 项目成员仓储
 *
 * @author Nick
 */
@Mapper
public interface ProjectMemberRepository extends BaseMapper<ProjectMember> {

    /**
     * 统计项目成员数量
     */
    @Select("SELECT COUNT(1) FROM prj_project_member WHERE project_id = #{projectId}")
    long countByProjectId(@Param("projectId") Long projectId);

    /**
     * 检查用户是否已是项目成员
     */
    @Select("SELECT COUNT(1) FROM prj_project_member WHERE project_id = #{projectId} AND user_id = #{userId}")
    long countByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);
}