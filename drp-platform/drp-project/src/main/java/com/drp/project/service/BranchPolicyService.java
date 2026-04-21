package com.drp.project.service;

import com.drp.project.dto.BranchPolicyCreateRequest;
import com.drp.project.dto.BranchPolicyDTO;
import com.drp.project.dto.BranchPolicyUpdateRequest;

import java.util.List;

/**
 * 分支策略服务接口
 *
 * @author Nick
 */
public interface BranchPolicyService {

    /**
     * 获取项目分支策略列表
     */
    List<BranchPolicyDTO> listByProjectId(Long projectId);

    /**
     * 创建分支策略
     */
    BranchPolicyDTO create(BranchPolicyCreateRequest request);

    /**
     * 更新分支策略
     */
    BranchPolicyDTO update(Long id, BranchPolicyUpdateRequest request);

    /**
     * 删除分支策略
     */
    void delete(Long id);

    /**
     * 统计项目分支策略数量
     */
    long countByProjectId(Long projectId);

    /**
     * 匹配分支策略
     *
     * @param projectId    项目ID
     * @param branchName    分支名称
     * @return 匹配的策略，未找到返回null
     */
    BranchPolicyDTO matchPolicy(Long projectId, String branchName);
}