package com.drp.project.service;

import com.drp.project.dto.EnvVariableCreateRequest;
import com.drp.project.dto.EnvVariableDTO;
import com.drp.project.dto.EnvVariableUpdateRequest;

import java.util.List;

/**
 * 环境变量服务接口
 *
 * @author Nick
 */
public interface EnvVariableService {

    /**
     * 获取项目环境变量列表
     *
     * @param projectId 项目ID
     * @param envCode   环境编码（可选）
     * @return 变量列表
     */
    List<EnvVariableDTO> listByProjectId(Long projectId, String envCode);

    /**
     * 创建环境变量
     */
    EnvVariableDTO create(EnvVariableCreateRequest request);

    /**
     * 更新环境变量
     */
    EnvVariableDTO update(Long id, EnvVariableUpdateRequest request);

    /**
     * 删除环境变量
     */
    void delete(Long id);

    /**
     * 统计项目环境变量数量
     */
    long countByProjectId(Long projectId);
}