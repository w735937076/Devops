package com.drp.project.controller;

import com.drp.common.result.Result;
import com.drp.project.service.GitService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Git 仓库控制器
 *
 * @author Nick
 */
@RestController
@RequestMapping("/api/projects/git")
public class GitController {

    private final GitService gitService;

    public GitController(GitService gitService) {
        this.gitService = gitService;
    }

    /**
     * 获取 Git 仓库分支列表
     *
     * @param gitUrl       Git 仓库地址 (HTTPS)
     * @param credentialId  凭证ID (可选)
     * @return 分支名称列表
     */
    @GetMapping("/branches")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<List<String>> fetchBranches(
            @RequestParam String gitUrl,
            @RequestParam(required = false) Long credentialId) {
        List<String> branches = gitService.fetchBranches(gitUrl, credentialId);
        return Result.success(branches);
    }
}
