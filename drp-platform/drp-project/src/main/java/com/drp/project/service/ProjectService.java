package com.drp.project.service;

import com.drp.common.dto.PageResponse;
import com.drp.project.dto.ProjectCreateRequest;
import com.drp.project.dto.ProjectDTO;
import com.drp.project.dto.ProjectQueryRequest;
import com.drp.project.dto.ProjectUpdateRequest;

/**
 * 项目管理服务
 *
 * @author Nick
 */
public interface ProjectService {

    PageResponse<ProjectDTO> queryPage(ProjectQueryRequest request);

    ProjectDTO getById(Long id);

    ProjectDTO create(ProjectCreateRequest request);

    ProjectDTO update(Long id, ProjectUpdateRequest request);

    void delete(Long id);
}