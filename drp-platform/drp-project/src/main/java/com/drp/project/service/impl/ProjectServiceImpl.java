package com.drp.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drp.common.dto.PageResponse;
import com.drp.common.exception.BusinessException;
import com.drp.project.dto.ProjectCreateRequest;
import com.drp.project.dto.ProjectDTO;
import com.drp.project.dto.ProjectQueryRequest;
import com.drp.project.dto.ProjectUpdateRequest;
import com.drp.project.entity.Credential;
import com.drp.project.entity.Project;
import com.drp.project.enums.ProjectType;
import com.drp.project.repository.CredentialRepository;
import com.drp.project.repository.ProjectRepository;
import com.drp.project.service.BranchPolicyService;
import com.drp.project.service.EnvVariableService;
import com.drp.project.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目管理服务实现
 *
 * @author Nick
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final CredentialRepository credentialRepository;
    private final EnvVariableService envVariableService;
    private final BranchPolicyService branchPolicyService;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              CredentialRepository credentialRepository,
                              EnvVariableService envVariableService,
                              BranchPolicyService branchPolicyService) {
        this.projectRepository = projectRepository;
        this.credentialRepository = credentialRepository;
        this.envVariableService = envVariableService;
        this.branchPolicyService = branchPolicyService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectDTO> queryPage(ProjectQueryRequest request) {
        request.validate();

        Page<Project> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(q -> q.like(Project::getName, request.getKeyword())
                    .or()
                    .like(Project::getCode, request.getKeyword())
                    .or()
                    .like(Project::getDescription, request.getKeyword()));
        }
        if (StringUtils.hasText(request.getType())) {
            validateProjectType(request.getType());
            wrapper.eq(Project::getType, request.getType());
        }
        if (request.getStatus() != null) {
            wrapper.eq(Project::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(Project::getId);

        Page<Project> result = projectRepository.selectPage(page, wrapper);
        List<ProjectDTO> records = result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResponse.of(records, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDTO getById(Long id) {
        Project project = requireProject(id);
        return toDTO(project);
    }

    @Override
    @Transactional
    public ProjectDTO create(ProjectCreateRequest request) {
        validateProjectType(request.getType());

        // 校验项目编码唯一性
        if (isCodeExists(request.getCode(), null)) {
            throw new BusinessException("项目编码已存在");
        }

        // 校验凭证是否存在
        validateCredential(request.getCredentialId());

        Project project = new Project();
        project.setName(request.getName());
        project.setCode(request.getCode());
        project.setType(request.getType());
        project.setDescription(request.getDescription());
        project.setGitUrl(request.getGitUrl());
        project.setCredentialId(request.getCredentialId());
        project.setDefaultBranch(StringUtils.hasText(request.getDefaultBranch()) ? request.getDefaultBranch() : "master");
        project.setBuildConfig(StringUtils.hasText(request.getBuildConfig()) ? request.getBuildConfig() : null);
        project.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        projectRepository.insert(project);
        log.info("创建项目成功 | id: {} | name: {} | code: {}", project.getId(), project.getName(), project.getCode());
        return toDTO(projectRepository.selectById(project.getId()));
    }

    @Override
    @Transactional
    public ProjectDTO update(Long id, ProjectUpdateRequest request) {
        validateProjectType(request.getType());

        Project project = requireProject(id);

        // 校验凭证是否存在
        validateCredential(request.getCredentialId());

        project.setName(request.getName());
        project.setType(request.getType());
        project.setDescription(request.getDescription());
        project.setGitUrl(request.getGitUrl());
        project.setCredentialId(request.getCredentialId());
        project.setDefaultBranch(request.getDefaultBranch());
        project.setBuildConfig(StringUtils.hasText(request.getBuildConfig()) ? request.getBuildConfig() : null);
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        projectRepository.updateById(project);
        log.info("更新项目成功 | id: {} | name: {}", project.getId(), project.getName());
        return toDTO(projectRepository.selectById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireProject(id);
        projectRepository.deleteById(id);
        log.info("删除项目成功 | id: {}", id);
    }

    private Project requireProject(Long id) {
        Project project = projectRepository.selectById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return project;
    }

    private void validateProjectType(String type) {
        if (!ProjectType.isValid(type)) {
            throw new BusinessException("不支持的项目类型: " + type);
        }
    }

    private void validateCredential(Long credentialId) {
        if (credentialId != null) {
            Credential credential = credentialRepository.selectById(credentialId);
            if (credential == null) {
                throw new BusinessException("关联的凭证不存在");
            }
        }
    }

    private boolean isCodeExists(String code, Long excludeId) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Project::getId, excludeId);
        }
        return projectRepository.selectCount(wrapper) > 0;
    }

    private ProjectDTO toDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setCode(project.getCode());
        dto.setType(project.getType());
        dto.setTypeDesc(ProjectType.valueOf(project.getType()).getDescription());
        dto.setDescription(project.getDescription());
        dto.setGitUrl(project.getGitUrl());
        dto.setCredentialId(project.getCredentialId());
        dto.setDefaultBranch(project.getDefaultBranch());
        dto.setBuildConfig(project.getBuildConfig());
        dto.setStatus(project.getStatus());
        dto.setStatusDesc(project.getStatus() != null && project.getStatus() == 1 ? "启用" : "禁用");
        dto.setCreateTime(project.getCreateTime());
        dto.setUpdateTime(project.getUpdateTime());

        // 填充凭证名称
        if (project.getCredentialId() != null) {
            Credential credential = credentialRepository.selectById(project.getCredentialId());
            if (credential != null) {
                dto.setCredentialName(credential.getName());
            }
        }

        // 填充统计数据
        dto.setVariableCount(envVariableService.countByProjectId(project.getId()));
        dto.setPolicyCount(branchPolicyService.countByProjectId(project.getId()));

        return dto;
    }
}