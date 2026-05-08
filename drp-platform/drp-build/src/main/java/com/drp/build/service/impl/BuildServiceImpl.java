package com.drp.build.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drp.build.dto.ArtifactDTO;
import com.drp.build.dto.BuildDetailDTO;
import com.drp.build.dto.BuildTriggerRequest;
import com.drp.build.entity.BuildRecord;
import com.drp.build.entity.Pipeline;
import com.drp.build.enums.BuildStatus;
import com.drp.build.enums.TriggerType;
import com.drp.build.repository.BuildRecordRepository;
import com.drp.build.repository.PipelineRepository;
import com.drp.build.service.BuildService;
import com.drp.build.service.PipelineService;
import com.drp.build.service.executor.BuildExecutor;
import com.drp.build.service.executor.ExecutorFactory;
import com.drp.common.dto.PageResponse;
import com.drp.common.exception.BusinessException;
import com.drp.project.entity.Project;
import com.drp.project.repository.ProjectRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BuildServiceImpl implements BuildService {

    private static final Logger log = LoggerFactory.getLogger(BuildServiceImpl.class);

    private final BuildRecordRepository buildRecordRepository;
    private final PipelineRepository pipelineRepository;
    private final ProjectRepository projectRepository;
    private final PipelineService pipelineService;
    private final ExecutorFactory executorFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    // WebSocket 会话管理 (buildId -> session)
    private final Map<Long, org.springframework.web.socket.WebSocketSession> logSessions = new ConcurrentHashMap<>();

    public BuildServiceImpl(BuildRecordRepository buildRecordRepository,
                           PipelineRepository pipelineRepository,
                           ProjectRepository projectRepository,
                           PipelineService pipelineService,
                           @Lazy ExecutorFactory executorFactory,
                           ApplicationEventPublisher eventPublisher,
                           ObjectMapper objectMapper,
                           StringRedisTemplate redisTemplate) {
        this.buildRecordRepository = buildRecordRepository;
        this.pipelineRepository = pipelineRepository;
        this.projectRepository = projectRepository;
        this.pipelineService = pipelineService;
        this.executorFactory = executorFactory;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public BuildDetailDTO triggerBuild(BuildTriggerRequest request, String triggerUser) {
        // 1. 校验项目
        Project project = projectRepository.selectById(request.getProjectId());
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        // 2. 获取或创建流水线
        Pipeline pipeline = getOrCreatePipeline(request.getProjectId(), request.getPipelineId());

        // 3. 生成构建号
        int buildNumber = buildRecordRepository.getMaxBuildNumber(request.getProjectId()) + 1;

        // 4. 创建构建记录
        BuildRecord record = new BuildRecord();
        record.setBuildNumber(buildNumber);
        record.setProjectId(request.getProjectId());
        record.setPipelineId(pipeline.getId());
        record.setBranch(request.getBranch());
        record.setCommitId(request.getCommitId() != null ? request.getCommitId() : "HEAD");
        record.setStatus(BuildStatus.QUEUED.getCode());
        record.setTriggerType(request.getTriggerType() != null ? request.getTriggerType() : TriggerType.MANUAL.name());
        record.setTriggerUser(triggerUser);
        record.setBuildConfig(project.getBuildConfig());

        buildRecordRepository.insert(record);

        // 5. 异步执行构建
        BuildExecutor executor = executorFactory.getExecutor(project.getType());
        executor.executeAsync(record, project, pipeline);

        log.info("触发构建成功 | buildId: {} | projectId: {} | branch: {}",
                record.getId(), request.getProjectId(), request.getBranch());

        return BuildDetailDTO.fromEntity(record);
    }

    private Pipeline getOrCreatePipeline(Long projectId, Long pipelineId) {
        if (pipelineId != null) {
            return pipelineService.getById(pipelineId);
        }

        // 查询默认流水线
        try {
            return pipelineService.getDefaultPipeline(projectId);
        } catch (BusinessException e) {
            // 创建默认流水线
            Pipeline pipeline = new Pipeline();
            pipeline.setProjectId(projectId);
            pipeline.setName("默认流水线");
            pipeline.setStages("[{\"name\":\"checkout\",\"type\":\"GIT_CLONE\",\"enabled\":true},{\"name\":\"build\",\"type\":\"MAVEN_BUILD\",\"enabled\":true}]");
            pipeline.setStatus(1);
            pipeline.setIsDefault(1);
            pipeline.setTimeout(3600);
            pipelineRepository.insert(pipeline);
            return pipeline;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BuildDetailDTO> queryPage(Long projectId, Integer status, String branch, int page, int size) {
        Page<BuildRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BuildRecord> wrapper = new LambdaQueryWrapper<>();

        if (projectId != null) {
            wrapper.eq(BuildRecord::getProjectId, projectId);
        }
        if (status != null) {
            wrapper.eq(BuildRecord::getStatus, status);
        }
        if (branch != null && !branch.isBlank()) {
            wrapper.eq(BuildRecord::getBranch, branch);
        }

        wrapper.orderByDesc(BuildRecord::getId);
        Page<BuildRecord> result = buildRecordRepository.selectPage(pageParam, wrapper);

        List<BuildDetailDTO> records = result.getRecords().stream()
                .map(this::toBuildDetailDTO)
                .toList();

        return PageResponse.of(records, result.getTotal(), page, size);
    }

    private BuildDetailDTO toBuildDetailDTO(BuildRecord record) {
        BuildDetailDTO dto = BuildDetailDTO.fromEntity(record);

        // 填充项目名称
        Project project = projectRepository.selectById(record.getProjectId());
        if (project != null) {
            dto.setProjectName(project.getName());
        }

        // 填充流水线信息
        if (record.getPipelineId() != null) {
            Pipeline pipeline = pipelineRepository.selectById(record.getPipelineId());
            if (pipeline != null) {
                dto.setPipelineInfo(pipeline);
            }
        }

        // 从构建配置填充构建参数
        if (record.getBuildConfig() != null && !record.getBuildConfig().isEmpty()) {
            dto.setBuildParamsFromConfig(record.getBuildConfig());
        }

        // 解析产物列表
        if (record.getArtifacts() != null) {
            try {
                List<ArtifactDTO> artifacts = objectMapper.readValue(
                        record.getArtifacts(), new TypeReference<List<ArtifactDTO>>() {});
                dto.setArtifacts(artifacts);
            } catch (Exception e) {
                log.warn("解析构建产物失败", e);
            }
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public BuildDetailDTO getBuildDetail(Long id) {
        BuildRecord record = buildRecordRepository.selectById(id);
        if (record == null) {
            throw new BusinessException("构建记录不存在");
        }
        return toBuildDetailDTO(record);
    }

    @Override
    @Transactional
    public void cancelBuild(Long id) {
        BuildRecord record = buildRecordRepository.selectById(id);
        if (record == null) {
            throw new BusinessException("构建记录不存在");
        }

        if (record.getStatus() != BuildStatus.QUEUED.getCode() &&
            record.getStatus() != BuildStatus.RUNNING.getCode()) {
            throw new BusinessException("当前状态不允许取消");
        }

        record.setStatus(BuildStatus.CANCELLED.getCode());
        record.setEndTime(LocalDateTime.now());
        if (record.getStartTime() != null) {
            record.setDuration((int) java.time.Duration.between(record.getStartTime(), record.getEndTime()).getSeconds());
        }
        buildRecordRepository.updateById(record);

        log.info("取消构建 | buildId: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public String getBuildLog(Long id) {
        // 从 Redis 获取日志
        String logKey = "build:log:" + id;
        String log = redisTemplate.opsForValue().get(logKey);
        return log != null ? log : "";
    }

    @Override
    public void pushBuildLog(Long buildId, String logContent) {
        // 存储到 Redis
        String logKey = "build:log:" + buildId;
        redisTemplate.opsForValue().append(logKey, logContent);

        // 发送到 WebSocket
        org.springframework.web.socket.WebSocketSession session = logSessions.get(buildId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new org.springframework.web.socket.TextMessage(logContent));
            } catch (Exception e) {
                log.error("推送构建日志失败", e);
            }
        }
    }

    @Override
    @Transactional
    public void finishBuild(Long buildId, int status, String errorMessage) {
        finishBuild(buildId, status, errorMessage, null);
    }

    @Override
    @Transactional
    public void finishBuild(Long buildId, int status, String errorMessage, String artifacts) {
        BuildRecord record = buildRecordRepository.selectById(buildId);
        if (record == null) {
            return;
        }

        record.setStatus(status);
        record.setEndTime(LocalDateTime.now());
        if (record.getStartTime() != null) {
            record.setDuration((int) java.time.Duration.between(record.getStartTime(), record.getEndTime()).getSeconds());
        }
        record.setErrorMessage(errorMessage);
        if (artifacts != null) {
            record.setArtifacts(artifacts);
        }
        buildRecordRepository.updateById(record);

        log.info("构建完成 | buildId: {} | status: {} | duration: {}s",
                buildId, status, record.getDuration());
    }

    // WebSocket 会话管理
    public void registerLogSession(Long buildId, org.springframework.web.socket.WebSocketSession session) {
        logSessions.put(buildId, session);
    }

    public void unregisterLogSession(Long buildId) {
        logSessions.remove(buildId);
    }

    @Override
    public String generateArtifacts(Project project, BuildRecord record, String workspace) {
        try {
            java.io.File workDir = new java.io.File(workspace);
            log.info("生成构建产物 | workspace: {} | type: {}", workDir.getAbsolutePath(), project.getType());
            java.util.List<ArtifactDTO> artifactList = new java.util.ArrayList<>();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            if ("JAVA_MAVEN".equals(project.getType())) {
                java.io.File targetDir = new java.io.File(workDir, "target");
                log.info("查找 Maven 产物 | targetDir: {} | exists: {}", targetDir.getAbsolutePath(), targetDir.exists());
                if (targetDir.exists()) {
                    // 查找 jar 文件
                    findArtifacts(targetDir, ".jar", artifactList, now);
                    // 查找 war 文件
                    findArtifacts(targetDir, ".war", artifactList, now);
                }
            } else if ("NODE".equals(project.getType())) {
                java.io.File distDir = new java.io.File(workDir, "dist");
                log.info("查找 Node 产物 | distDir: {} | exists: {}", distDir.getAbsolutePath(), distDir.exists());
                if (distDir.exists()) {
                    findAllFiles(distDir, distDir, artifactList, now);
                }
            } else if ("PYTHON".equals(project.getType())) {
                java.io.File distDir = new java.io.File(workDir, "dist");
                log.info("查找 Python 产物 | distDir: {} | exists: {}", distDir.getAbsolutePath(), distDir.exists());
                if (distDir.exists()) {
                    findAllFiles(distDir, distDir, artifactList, now);
                }
            }

            log.info("找到 {} 个产物", artifactList.size());
            if (artifactList.isEmpty()) {
                return null;
            }

            for (ArtifactDTO artifact : artifactList) {
                String pathToEncode = artifact.getPath().replace("\\", "/");
                String encodedPath = java.net.URLEncoder.encode(
                        pathToEncode, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
                artifact.setDownloadUrl(
                        "/api/builds/" + record.getId() + "/artifact/download?path=" + encodedPath);
            }

            return objectMapper.writeValueAsString(artifactList);
        } catch (Exception e) {
            log.error("生成构建产物失败", e);
            return null;
        }
    }

    private void findArtifacts(java.io.File dir, String extension, java.util.List<ArtifactDTO> artifactList, java.time.LocalDateTime createTime) {
        if (dir == null || !dir.exists()) return;
        java.io.File[] files = dir.listFiles();
        if (files == null) return;
        for (java.io.File file : files) {
            if (file.isDirectory()) {
                String name = file.getName().toLowerCase();
                if (!name.equals("maven-archiver") && !name.equals("maven-status") && !name.equals("generated-sources") && !name.equals("generated-test-sources")) {
                    findArtifacts(file, extension, artifactList, createTime);
                }
            } else if (file.getName().endsWith(extension)) {
                ArtifactDTO artifact = new ArtifactDTO();
                artifact.setName(file.getName());
                artifact.setPath(file.getAbsolutePath());
                artifact.setSize(file.length());
                artifact.setCreateTime(createTime);
                artifactList.add(artifact);
            }
        }
    }

    private void findAllFiles(java.io.File rootDir, java.io.File dir, java.util.List<ArtifactDTO> artifactList, java.time.LocalDateTime createTime) {
        if (dir == null || !dir.exists()) return;
        java.io.File[] files = dir.listFiles();
        if (files == null) return;
        for (java.io.File file : files) {
            if (file.isDirectory()) {
                findAllFiles(rootDir, file, artifactList, createTime);
            } else {
                ArtifactDTO artifact = new ArtifactDTO();
                java.nio.file.Path root = rootDir.toPath();
                java.nio.file.Path current = file.toPath();
                String relative = root.relativize(current).toString().replace("\\", "/");
                artifact.setName(relative);
                artifact.setPath(file.getAbsolutePath());
                artifact.setSize(file.length());
                artifact.setCreateTime(createTime);
                artifactList.add(artifact);
            }
        }
    }
}
