package com.drp.project.service;

import java.util.List;

/**
 * Git 仓库服务
 *
 * @author Nick
 */
public interface GitService {

    /**
     * 从 Git 仓库获取分支列表
     *
     * @param gitUrl       Git 仓库地址 (HTTPS)
     * @param credentialId 凭证ID (可选，用于私有仓库认证)
     * @return 分支名称列表
     */
    List<String> fetchBranches(String gitUrl, Long credentialId);
}
