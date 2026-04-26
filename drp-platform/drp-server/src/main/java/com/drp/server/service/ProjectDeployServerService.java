package com.drp.server.service;

import com.drp.server.dto.ProjectDeployServerCreateRequest;
import com.drp.server.dto.ProjectDeployServerDTO;
import com.drp.server.dto.ProjectDeployServerUpdateRequest;

import java.util.List;

public interface ProjectDeployServerService {

    List<ProjectDeployServerDTO> listByProjectId(Long projectId, String env);

    ProjectDeployServerDTO create(ProjectDeployServerCreateRequest request);

    ProjectDeployServerDTO update(Long projectId, Long id, ProjectDeployServerUpdateRequest request);

    void delete(Long projectId, Long id);
}
