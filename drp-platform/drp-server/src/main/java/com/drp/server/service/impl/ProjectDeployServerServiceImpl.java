package com.drp.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drp.common.exception.BusinessException;
import com.drp.project.entity.Project;
import com.drp.project.repository.ProjectRepository;
import com.drp.server.dto.ProjectDeployServerCreateRequest;
import com.drp.server.dto.ProjectDeployServerDTO;
import com.drp.server.dto.ProjectDeployServerUpdateRequest;
import com.drp.server.entity.ProjectServer;
import com.drp.server.entity.Server;
import com.drp.server.enums.ServerStatus;
import com.drp.server.repository.ProjectServerRepository;
import com.drp.server.repository.ServerRepository;
import com.drp.server.service.ProjectDeployServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectDeployServerServiceImpl implements ProjectDeployServerService {

    private static final List<String> SUPPORTED_ENVS = List.of("dev", "test", "pre", "prod");

    private final ProjectRepository projectRepository;
    private final ProjectServerRepository projectServerRepository;
    private final ServerRepository serverRepository;

    @Override
    public List<ProjectDeployServerDTO> listByProjectId(Long projectId, String env) {
        validateProject(projectId);

        LambdaQueryWrapper<ProjectServer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectServer::getProjectId, projectId)
                .eq(StringUtils.hasText(env), ProjectServer::getEnv, env)
                .eq(ProjectServer::getDeleted, false)
                .orderByAsc(ProjectServer::getEnv, ProjectServer::getId);

        List<ProjectServer> mappings = projectServerRepository.selectList(wrapper);
        Map<Integer, Server> serverMap = loadServerMap(mappings);
        return mappings.stream()
                .map(item -> toDTO(item, serverMap.get(Math.toIntExact(item.getServerId()))))
                .toList();
    }

    @Override
    @Transactional
    public ProjectDeployServerDTO create(ProjectDeployServerCreateRequest request) {
        Long projectId = request.getProjectId();
        validateProject(projectId);
        String env = normalizeEnv(request.getEnv());
        Server server = validateServer(request.getServerId());
        ensureUnique(projectId, request.getServerId(), env, null);

        ProjectServer mapping = new ProjectServer();
        mapping.setProjectId(projectId);
        mapping.setServerId(request.getServerId());
        mapping.setEnv(env);
        mapping.setDeployPath(request.getDeployPath().trim());
        mapping.setDeleted(false);
        projectServerRepository.insert(mapping);
        return toDTO(mapping, server);
    }

    @Override
    @Transactional
    public ProjectDeployServerDTO update(Long projectId, Long id, ProjectDeployServerUpdateRequest request) {
        validateProject(projectId);
        ProjectServer mapping = getMapping(projectId, id);
        String env = normalizeEnv(request.getEnv());
        Server server = validateServer(request.getServerId());
        ensureUnique(projectId, request.getServerId(), env, id);

        mapping.setServerId(request.getServerId());
        mapping.setEnv(env);
        mapping.setDeployPath(request.getDeployPath().trim());
        projectServerRepository.updateById(mapping);
        return toDTO(mapping, server);
    }

    @Override
    @Transactional
    public void delete(Long projectId, Long id) {
        validateProject(projectId);
        ProjectServer mapping = getMapping(projectId, id);
        projectServerRepository.deleteById(mapping.getId());
    }

    private void validateProject(Long projectId) {
        if (projectId == null) {
            throw new BusinessException("项目不能为空");
        }
        Project project = projectRepository.selectById(projectId);
        if (project == null || Boolean.TRUE.equals(project.getDeleted())) {
            throw new BusinessException("项目不存在");
        }
    }

    private Server validateServer(Long serverId) {
        if (serverId == null) {
            throw new BusinessException("服务器不能为空");
        }
        Server server = serverRepository.selectById(Math.toIntExact(serverId));
        if (server == null || Boolean.TRUE.equals(server.getDeleted())) {
            throw new BusinessException("服务器不存在");
        }
        return server;
    }

    private String normalizeEnv(String env) {
        if (!StringUtils.hasText(env)) {
            throw new BusinessException("部署环境不能为空");
        }
        String normalized = env.trim().toLowerCase();
        if (!SUPPORTED_ENVS.contains(normalized)) {
            throw new BusinessException("不支持的部署环境");
        }
        return normalized;
    }

    private void ensureUnique(Long projectId, Long serverId, String env, Long excludeId) {
        LambdaQueryWrapper<ProjectServer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectServer::getProjectId, projectId)
                .eq(ProjectServer::getServerId, serverId)
                .eq(ProjectServer::getEnv, env)
                .eq(ProjectServer::getDeleted, false)
                .ne(excludeId != null, ProjectServer::getId, excludeId);
        if (projectServerRepository.selectCount(wrapper) > 0) {
            throw new BusinessException("该环境下已绑定当前服务器，请勿重复添加");
        }
    }

    private ProjectServer getMapping(Long projectId, Long id) {
        ProjectServer mapping = projectServerRepository.selectById(id);
        if (mapping == null || Boolean.TRUE.equals(mapping.getDeleted()) || !Objects.equals(mapping.getProjectId(), projectId)) {
            throw new BusinessException("项目部署服务器绑定不存在");
        }
        return mapping;
    }

    private Map<Integer, Server> loadServerMap(List<ProjectServer> mappings) {
        List<Integer> serverIds = mappings.stream()
                .map(ProjectServer::getServerId)
                .filter(Objects::nonNull)
                .map(Math::toIntExact)
                .distinct()
                .toList();
        if (serverIds.isEmpty()) {
            return Map.of();
        }
        return serverRepository.selectBatchIds(serverIds).stream()
                .collect(Collectors.toMap(Server::getId, Function.identity(), (a, b) -> a));
    }

    private ProjectDeployServerDTO toDTO(ProjectServer mapping, Server server) {
        ProjectDeployServerDTO dto = new ProjectDeployServerDTO();
        dto.setId(mapping.getId());
        dto.setProjectId(mapping.getProjectId());
        dto.setServerId(mapping.getServerId());
        dto.setEnv(mapping.getEnv());
        dto.setEnvName(getEnvName(mapping.getEnv()));
        dto.setDeployPath(mapping.getDeployPath());
        dto.setCreateTime(mapping.getCreateTime());
        if (server != null) {
            dto.setServerName(server.getName());
            dto.setHostname(server.getHostname());
            dto.setPort(server.getPort());
            dto.setServerEnv(server.getEnv());
            dto.setServerStatus(server.getStatus());
            dto.setServerStatusDesc(ServerStatus.fromValue(server.getStatus()).getDesc());
        }
        return dto;
    }

    private String getEnvName(String env) {
        return switch (env) {
            case "dev" -> "开发环境";
            case "test" -> "测试环境";
            case "pre" -> "预发环境";
            case "prod" -> "生产环境";
            default -> env;
        };
    }
}
