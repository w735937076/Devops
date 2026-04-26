package com.drp.deploy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drp.build.dto.BuildDetailDTO;
import com.drp.build.entity.BuildRecord;
import com.drp.build.repository.BuildRecordRepository;
import com.drp.common.dto.PageResponse;
import com.drp.common.exception.BusinessException;
import com.drp.deploy.dto.*;
import com.drp.deploy.entity.ApprovalRecord;
import com.drp.deploy.entity.DeployRecord;
import com.drp.deploy.entity.DeployStep;
import com.drp.deploy.enums.ApprovalStatus;
import com.drp.deploy.enums.DeployStatus;
import com.drp.deploy.enums.RiskLevel;
import com.drp.deploy.executor.DeployExecutor;
import com.drp.deploy.repository.ApprovalRecordRepository;
import com.drp.deploy.repository.DeployRecordRepository;
import com.drp.deploy.repository.DeployStepRepository;
import com.drp.deploy.service.DeployService;
import com.drp.project.dto.BranchPolicyDTO;
import com.drp.project.entity.Project;
import com.drp.project.repository.ProjectRepository;
import com.drp.project.service.BranchPolicyService;
import com.drp.server.entity.ProjectServer;
import com.drp.server.entity.Server;
import com.drp.server.repository.ProjectServerRepository;
import com.drp.server.repository.ServerRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeployServiceImpl implements DeployService {

    private final DeployRecordRepository deployRecordRepository;
    private final DeployStepRepository deployStepRepository;
    private final ApprovalRecordRepository approvalRecordRepository;
    private final ProjectRepository projectRepository;
    private final BuildRecordRepository buildRecordRepository;
    private final ProjectServerRepository projectServerRepository;
    private final ServerRepository serverRepository;
    private final BranchPolicyService branchPolicyService;
    private final ObjectMapper objectMapper;
    private final DeployExecutor deployExecutor;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeploySummaryDTO> queryPage(DeployQueryRequest request) {
        Page<DeployRecord> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<DeployRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getProjectId() != null, DeployRecord::getProjectId, request.getProjectId())
                .eq(StringUtils.hasText(request.getEnv()), DeployRecord::getEnv, request.getEnv())
                .eq(request.getStatus() != null, DeployRecord::getStatus, request.getStatus())
                .eq(StringUtils.hasText(request.getApprovalStatus()), DeployRecord::getApprovalStatus, request.getApprovalStatus())
                .eq(StringUtils.hasText(request.getRiskLevel()), DeployRecord::getRiskLevel, request.getRiskLevel())
                .eq(StringUtils.hasText(request.getTriggerType()), DeployRecord::getTriggerType, request.getTriggerType())
                .orderByDesc(DeployRecord::getId);

        Page<DeployRecord> result = deployRecordRepository.selectPage(page, wrapper);
        List<DeploySummaryDTO> records = result.getRecords().stream().map(this::toSummary).toList();
        return PageResponse.of(records, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public DeployDetailDTO getDetail(Long id) {
        DeployRecord record = getRecord(id);
        DeployDetailDTO detail = toDetail(record);
        detail.setSteps(listSteps(id));
        detail.setApprovalRecords(listApprovalRecords(id));
        detail.setHealthChecks(parseHealthChecks(record.getHealthChecks()));
        return detail;
    }

    @Override
    @Transactional(readOnly = true)
    public DeployPreviewDTO preview(DeployPreviewRequest request, String operator) {
        PreviewContext context = buildPreviewContext(
                request.getProjectId(),
                request.getBuildId(),
                request.getEnv(),
                request.getStrategy(),
                request.getServerIds(),
                request.getRequireApproval(),
                request.getDeployWindow(),
                request.getGrayPercent(),
                request.getGrayInterval(),
                request.getTriggerType(),
                request.getChangeTicket(),
                request.getAutoRollback(),
                operator
        );
        return context.preview;
    }

    @Override
    @Transactional
    public DeployDetailDTO create(DeployCreateRequest request, String operator) {
        PreviewContext context = buildPreviewContext(
                request.getProjectId(),
                request.getBuildId(),
                request.getEnv(),
                request.getStrategy(),
                request.getServerIds(),
                request.getRequireApproval(),
                request.getDeployWindow(),
                request.getGrayPercent(),
                request.getGrayInterval(),
                request.getTriggerType(),
                request.getChangeTicket(),
                request.getAutoRollback(),
                operator
        );

        if (Boolean.TRUE.equals(context.preview.getHasRunningDeploy())) {
            throw new BusinessException("当前项目环境存在运行中或待审批部署，请稍后重试");
        }
        if (Boolean.FALSE.equals(context.preview.getWindowValid()) && "prod".equalsIgnoreCase(request.getEnv())) {
            throw new BusinessException("当前不在生产发布窗口内，禁止直接部署");
        }
        long failCount = context.preview.getChecks().stream().filter(item -> "FAIL".equals(item.getStatus())).count();
        if (failCount > 0) {
            throw new BusinessException("部署预检未通过，请先处理失败项");
        }

        DeployRecord record = initRecordFromContext(context, operator);
        deployRecordRepository.insert(record);

        if (context.requireApproval) {
            record.setStatus(DeployStatus.WAIT_APPROVAL.getCode());
            record.setApprovalStatus(ApprovalStatus.PENDING.getCode());
            record.setCurrentStep("等待审批");
            record.setSummary("部署已创建，等待审批通过后执行");
            deployRecordRepository.updateById(record);
            createApprovalRecord(record);
        } else {
            executeDeployment(record, context, false);
        }

        return getDetail(record.getId());
    }

    @Override
    @Transactional
    public void cancel(Long id, String operator) {
        DeployRecord record = getRecord(id);
        DeployStatus status = DeployStatus.fromCode(record.getStatus());
        if (status != DeployStatus.WAIT_APPROVAL && status != DeployStatus.PENDING && status != DeployStatus.RUNNING) {
            throw new BusinessException("当前状态不支持取消");
        }
        record.setStatus(DeployStatus.CANCELLED.getCode());
        record.setCurrentStep("已取消");
        record.setEndTime(LocalDateTime.now());
        if (record.getStartTime() != null) {
            record.setDuration((int) Duration.between(record.getStartTime(), record.getEndTime()).getSeconds());
        }
        record.setSummary("部署已被 " + operator + " 取消");
        record.setLogContent(appendLog(record.getLogContent(), "[CANCEL] 操作人 " + operator + " 取消部署"));
        deployRecordRepository.updateById(record);
    }

    @Override
    @Transactional
    public DeployDetailDTO retry(Long id, String operator) {
        DeployRecord original = getRecord(id);
        DeployStatus status = DeployStatus.fromCode(original.getStatus());
        if (status != DeployStatus.FAILED && status != DeployStatus.CANCELLED && status != DeployStatus.REJECTED) {
            throw new BusinessException("仅失败、取消或驳回的部署可以重试");
        }

        PreviewContext context = buildPreviewContext(
                original.getProjectId(),
                original.getBuildId(),
                original.getEnv(),
                normalizeStrategy(original.getStrategy()),
                parseLongList(original.getServerIds()),
                ApprovalStatus.PENDING.getCode().equals(original.getApprovalStatus()),
                original.getDeployWindow(),
                parseGrayPercent(original.getGrayConfig()),
                parseGrayInterval(original.getGrayConfig()),
                original.getTriggerType(),
                original.getChangeTicket(),
                original.getAutoRollback(),
                operator
        );

        DeployRecord retry = initRecordFromContext(context, operator);
        retry.setSummary("基于部署 #" + original.getId() + " 重试");
        retry.setLogContent(appendLog("", "[RETRY] 基于部署 #" + original.getId() + " 发起重试"));
        deployRecordRepository.insert(retry);
        if (context.requireApproval) {
            retry.setStatus(DeployStatus.WAIT_APPROVAL.getCode());
            retry.setApprovalStatus(ApprovalStatus.PENDING.getCode());
            retry.setCurrentStep("等待审批");
            deployRecordRepository.updateById(retry);
            createApprovalRecord(retry);
        } else {
            executeDeployment(retry, context, false);
        }
        return getDetail(retry.getId());
    }

    @Override
    @Transactional
    public DeployDetailDTO rollback(Long id, String operator, String reason) {
        DeployRecord current = getRecord(id);
        if (DeployStatus.fromCode(current.getStatus()) != DeployStatus.SUCCESS) {
            throw new BusinessException("仅成功部署支持回滚");
        }

        DeployRecord target = findRollbackTarget(current);
        if (target == null) {
            throw new BusinessException("未找到可回滚的稳定版本");
        }

        PreviewContext context = buildPreviewContext(
                current.getProjectId(),
                target.getBuildId(),
                current.getEnv(),
                "ROLLBACK",
                parseLongList(current.getServerIds()),
                false,
                current.getDeployWindow(),
                null,
                null,
                "MANUAL",
                current.getChangeTicket(),
                true,
                operator
        );

        DeployRecord rollback = initRecordFromContext(context, operator);
        rollback.setRollbackFromDeployId(current.getId());
        rollback.setSummary("回滚到稳定版本 " + target.getVersion() + (StringUtils.hasText(reason) ? "，原因：" + reason : ""));
        rollback.setLogContent(appendLog("", "[ROLLBACK] 从部署 #" + current.getId() + " 回滚到版本 " + target.getVersion()));
        deployRecordRepository.insert(rollback);
        executeDeployment(rollback, context, true);
        return getDetail(rollback.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public String getLog(Long id) {
        return getRecord(id).getLogContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeployCheckItemDTO> getHealthChecks(Long id) {
        return parseHealthChecks(getRecord(id).getHealthChecks());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeploySummaryDTO> getPendingApprovals() {
        LambdaQueryWrapper<DeployRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeployRecord::getApprovalStatus, ApprovalStatus.PENDING.getCode())
                .orderByDesc(DeployRecord::getId)
                .last("limit 20");
        return deployRecordRepository.selectList(wrapper).stream().map(this::toSummary).toList();
    }

    @Override
    @Transactional
    public DeployDetailDTO approve(Long id, String approver, String comment) {
        DeployRecord record = getRecord(id);
        if (!ApprovalStatus.PENDING.getCode().equals(record.getApprovalStatus())) {
            throw new BusinessException("当前部署不在待审批状态");
        }

        ApprovalRecord approvalRecord = getLatestApprovalRecord(id);
        approvalRecord.setStatus(ApprovalStatus.APPROVED.getCode());
        approvalRecord.setApprover(approver);
        approvalRecord.setComment(comment);
        approvalRecord.setApproveTime(LocalDateTime.now());
        approvalRecordRepository.updateById(approvalRecord);

        record.setApprovalStatus(ApprovalStatus.APPROVED.getCode());
        record.setStatus(DeployStatus.RUNNING.getCode());
        record.setLogContent(appendLog(record.getLogContent(), "[APPROVAL] " + approver + " 审批通过" + suffixComment(comment)));
        deployRecordRepository.updateById(record);

        PreviewContext context = buildPreviewContext(
                record.getProjectId(),
                record.getBuildId(),
                record.getEnv(),
                normalizeStrategy(record.getStrategy()),
                parseLongList(record.getServerIds()),
                false,
                record.getDeployWindow(),
                parseGrayPercent(record.getGrayConfig()),
                parseGrayInterval(record.getGrayConfig()),
                record.getTriggerType(),
                record.getChangeTicket(),
                record.getAutoRollback(),
                approver
        );
        executeDeployment(record, context, false);
        return getDetail(id);
    }

    @Override
    @Transactional
    public DeployDetailDTO reject(Long id, String approver, String comment) {
        DeployRecord record = getRecord(id);
        if (!ApprovalStatus.PENDING.getCode().equals(record.getApprovalStatus())) {
            throw new BusinessException("当前部署不在待审批状态");
        }

        ApprovalRecord approvalRecord = getLatestApprovalRecord(id);
        approvalRecord.setStatus(ApprovalStatus.REJECTED.getCode());
        approvalRecord.setApprover(approver);
        approvalRecord.setComment(comment);
        approvalRecord.setApproveTime(LocalDateTime.now());
        approvalRecordRepository.updateById(approvalRecord);

        record.setStatus(DeployStatus.REJECTED.getCode());
        record.setApprovalStatus(ApprovalStatus.REJECTED.getCode());
        record.setCurrentStep("审批驳回");
        record.setEndTime(LocalDateTime.now());
        record.setSummary("部署已被驳回");
        record.setLogContent(appendLog(record.getLogContent(), "[APPROVAL] " + approver + " 驳回部署" + suffixComment(comment)));
        deployRecordRepository.updateById(record);
        return getDetail(id);
    }

    private PreviewContext buildPreviewContext(Long projectId,
                                               Long buildId,
                                               String env,
                                               String strategy,
                                               List<Long> serverIds,
                                               Boolean requireApproval,
                                               String deployWindow,
                                               Integer grayPercent,
                                               Integer grayInterval,
                                               String triggerType,
                                               String changeTicket,
                                               Boolean autoRollback,
                                               String operator) {
        if (projectId == null || buildId == null || !StringUtils.hasText(env) || !StringUtils.hasText(strategy)) {
            throw new BusinessException("部署预览参数不完整");
        }

        Project project = projectRepository.selectById(projectId);
        if (project == null || Boolean.TRUE.equals(project.getDeleted())) {
            throw new BusinessException("项目不存在");
        }

        BuildRecord buildRecord = buildRecordRepository.selectById(buildId);
        if (buildRecord == null || !Objects.equals(buildRecord.getProjectId(), projectId)) {
            throw new BusinessException("构建记录不存在或不属于当前项目");
        }
        if (buildRecord.getStatus() == null || buildRecord.getStatus() != 2) {
            throw new BusinessException("仅成功构建版本允许部署");
        }

        List<ProjectServer> mappings = projectServerRepository.selectList(
                new QueryWrapper<ProjectServer>().eq("project_id", projectId).eq("env", env).eq("deleted", 0)
        );
        if (serverIds != null && !serverIds.isEmpty()) {
            mappings = mappings.stream().filter(item -> serverIds.contains(item.getServerId())).toList();
        }
        if (mappings.isEmpty()) {
            throw new BusinessException("当前项目环境未绑定部署服务器");
        }

        List<Integer> targetServerIds = mappings.stream()
                .map(ProjectServer::getServerId)
                .filter(Objects::nonNull)
                .map(Long::intValue)
                .distinct()
                .toList();
        List<Server> servers = targetServerIds.isEmpty() ? List.of() : serverRepository.selectBatchIds(targetServerIds);
        Map<Integer, ProjectServer> mappingMap = mappings.stream()
                .collect(Collectors.toMap(item -> item.getServerId().intValue(), item -> item, (a, b) -> a));

        BranchPolicyDTO policy = branchPolicyService.matchPolicy(projectId, buildRecord.getBranch());
        boolean approvalRequired = "prod".equalsIgnoreCase(env)
                || Boolean.TRUE.equals(requireApproval)
                || (policy != null && Integer.valueOf(1).equals(policy.getRequireApproval()));
        boolean windowValid = isWindowValid(deployWindow);
        RiskLevel riskLevel = evaluateRisk(env, strategy, grayPercent, servers.size());
        boolean hasRunning = hasRunningDeploy(projectId, env);

        List<DeployServerDTO> serverDTOs = servers.stream()
                .sorted(Comparator.comparing(Server::getId))
                .map(server -> toServerDTO(server, mappingMap.get(server.getId())))
                .toList();
        List<DeployCheckItemDTO> checks = buildCheckItems(project, buildRecord, env, deployWindow, windowValid, policy, hasRunning, serverDTOs);

        DeployPreviewDTO preview = new DeployPreviewDTO();
        preview.setProjectId(projectId);
        preview.setProjectName(project.getName());
        preview.setBuildId(buildId);
        preview.setBuildNumber(buildRecord.getBuildNumber());
        preview.setVersion(BuildDetailDTO.fromEntity(buildRecord).getVersion());
        preview.setBranch(buildRecord.getBranch());
        preview.setEnv(env);
        preview.setStrategy(displayStrategy(strategy));
        preview.setRiskLevel(riskLevel.getCode());
        preview.setRiskLevelDesc(riskLevel.getDescription());
        preview.setRequireApproval(approvalRequired);
        preview.setWindowValid(windowValid);
        preview.setDeployWindow(deployWindow);
        preview.setGrayPercent(grayPercent);
        preview.setGrayInterval(grayInterval);
        preview.setHasRunningDeploy(hasRunning);
        preview.setServers(serverDTOs);
        preview.setChecks(checks);
        preview.setWarnings(buildWarnings(approvalRequired, windowValid, hasRunning, riskLevel, changeTicket));

        PreviewContext context = new PreviewContext();
        context.project = project;
        context.buildRecord = buildRecord;
        context.preview = preview;
        context.requireApproval = approvalRequired;
        context.deployWindow = deployWindow;
        context.grayPercent = grayPercent;
        context.grayInterval = grayInterval;
        context.triggerType = StringUtils.hasText(triggerType) ? triggerType : "MANUAL";
        context.changeTicket = changeTicket;
        context.autoRollback = autoRollback == null || autoRollback;
        context.operator = operator;
        return context;
    }

    private DeployRecord initRecordFromContext(PreviewContext context, String operator) {
        DeployRecord record = new DeployRecord();
        record.setProjectId(context.project.getId());
        record.setBuildId(context.buildRecord.getId());
        record.setEnv(context.preview.getEnv());
        record.setStrategy(context.preview.getStrategy());
        record.setStatus(context.requireApproval ? DeployStatus.WAIT_APPROVAL.getCode() : DeployStatus.RUNNING.getCode());
        record.setApprovalStatus(context.requireApproval ? ApprovalStatus.PENDING.getCode() : ApprovalStatus.NOT_REQUIRED.getCode());
        record.setRiskLevel(context.preview.getRiskLevel());
        record.setTriggerType(context.triggerType);
        record.setTriggerUser(operator);
        record.setVersion(context.preview.getVersion());
        record.setBranch(context.preview.getBranch());
        record.setServerIds(context.preview.getServers().stream().map(item -> String.valueOf(item.getId())).collect(Collectors.joining(",")));
        record.setServerSnapshot(writeJson(context.preview.getServers()));
        record.setDeployWindow(context.deployWindow);
        record.setGrayConfig(writeJson(Map.of(
                "grayPercent", context.grayPercent,
                "grayInterval", context.grayInterval
        )));
        record.setChangeTicket(context.changeTicket);
        record.setWindowValid(context.preview.getWindowValid());
        record.setAutoRollback(context.autoRollback);
        record.setCurrentStep(context.requireApproval ? "等待审批" : "预检");
        record.setLogContent(appendLog("", "[CREATE] " + operator + " 创建部署任务，目标版本 " + context.preview.getVersion()));
        record.setHealthChecks(writeJson(context.preview.getChecks()));
        record.setSummary(context.requireApproval ? "等待审批" : "部署执行中");
        return record;
    }

    private void createApprovalRecord(DeployRecord record) {
        ApprovalRecord approvalRecord = new ApprovalRecord();
        approvalRecord.setDeployId(record.getId());
        approvalRecord.setProjectId(record.getProjectId());
        approvalRecord.setEnv(record.getEnv());
        approvalRecord.setStatus(ApprovalStatus.PENDING.getCode());
        approvalRecordRepository.insert(approvalRecord);
    }

    private void executeDeployment(DeployRecord record, PreviewContext context, boolean rollbackMode) {
        try {
            LocalDateTime startTime = LocalDateTime.now();
            record.setStartTime(startTime);
            record.setLogContent(appendLog(record.getLogContent(),
                    "[EXEC] 开始执行部署 | strategy=" + context.preview.getStrategy() + " | rollback=" + rollbackMode));
            deployRecordRepository.updateById(record);

            deployExecutor.executeDeploy(record, context.preview.getServers(),
                    rollbackMode ? "ROLLBACK" : context.preview.getStrategy(), rollbackMode);

        } catch (Exception ex) {
            log.error("执行部署失败 | deployId: {}", record.getId(), ex);
            record.setStatus(DeployStatus.FAILED.getCode());
            record.setCurrentStep("执行失败");
            record.setEndTime(LocalDateTime.now());
            record.setErrorMessage(ex.getMessage());
            record.setSummary("部署执行失败");
            record.setLogContent(appendLog(record.getLogContent(), "[ERROR] " + ex.getMessage()));
            deployRecordRepository.updateById(record);
            throw ex instanceof BusinessException ? (BusinessException) ex : new BusinessException("执行部署失败: " + ex.getMessage());
        }
    }

    private DeployRecord findRollbackTarget(DeployRecord current) {
        LambdaQueryWrapper<DeployRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeployRecord::getProjectId, current.getProjectId())
                .eq(DeployRecord::getEnv, current.getEnv())
                .eq(DeployRecord::getStatus, DeployStatus.SUCCESS.getCode())
                .lt(DeployRecord::getId, current.getId())
                .orderByDesc(DeployRecord::getId)
                .last("limit 1");
        return deployRecordRepository.selectOne(wrapper);
    }

    private ApprovalRecord getLatestApprovalRecord(Long deployId) {
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalRecord::getDeployId, deployId).orderByDesc(ApprovalRecord::getId).last("limit 1");
        ApprovalRecord record = approvalRecordRepository.selectOne(wrapper);
        if (record == null) {
            throw new BusinessException("审批记录不存在");
        }
        return record;
    }

    private DeployRecord getRecord(Long id) {
        DeployRecord record = deployRecordRepository.selectById(id);
        if (record == null) {
            throw new BusinessException("部署记录不存在");
        }
        return record;
    }

    private DeploySummaryDTO toSummary(DeployRecord record) {
        DeploySummaryDTO dto = new DeploySummaryDTO();
        dto.setId(record.getId());
        dto.setProjectId(record.getProjectId());
        dto.setBuildId(record.getBuildId());
        dto.setVersion(record.getVersion());
        dto.setBranch(record.getBranch());
        dto.setEnv(record.getEnv());
        dto.setStrategy(record.getStrategy());
        dto.setStatus(record.getStatus());
        dto.setStatusDesc(DeployStatus.fromCode(record.getStatus()).getDescription());
        dto.setApprovalStatus(record.getApprovalStatus());
        dto.setApprovalStatusDesc(ApprovalStatus.fromCode(record.getApprovalStatus()).getDescription());
        dto.setRiskLevel(record.getRiskLevel());
        dto.setRiskLevelDesc(RiskLevel.fromCode(record.getRiskLevel()).getDescription());
        dto.setTriggerType(record.getTriggerType());
        dto.setTriggerTypeDesc(displayTriggerType(record.getTriggerType()));
        dto.setTriggerUser(record.getTriggerUser());
        dto.setCurrentStep(record.getCurrentStep());
        dto.setDuration(record.getDuration());
        dto.setStartTime(record.getStartTime());
        dto.setEndTime(record.getEndTime());
        dto.setCreateTime(record.getCreateTime());
        dto.setUpdateTime(record.getUpdateTime());

        Project project = projectRepository.selectById(record.getProjectId());
        if (project != null) {
            dto.setProjectName(project.getName());
        }

        BuildRecord buildRecord = buildRecordRepository.selectById(record.getBuildId());
        if (buildRecord != null) {
            dto.setBuildNumber(buildRecord.getBuildNumber());
        }

        dto.setServerNames(parseServers(record.getServerSnapshot()).stream()
                .map(DeployServerDTO::getName)
                .filter(StringUtils::hasText)
                .toList());
        return dto;
    }

    private DeployDetailDTO toDetail(DeployRecord record) {
        DeployDetailDTO dto = new DeployDetailDTO();
        DeploySummaryDTO summary = toSummary(record);
        dto.setId(summary.getId());
        dto.setProjectId(summary.getProjectId());
        dto.setProjectName(summary.getProjectName());
        dto.setBuildId(summary.getBuildId());
        dto.setBuildNumber(summary.getBuildNumber());
        dto.setVersion(summary.getVersion());
        dto.setBranch(summary.getBranch());
        dto.setEnv(summary.getEnv());
        dto.setStrategy(summary.getStrategy());
        dto.setStatus(summary.getStatus());
        dto.setStatusDesc(summary.getStatusDesc());
        dto.setApprovalStatus(summary.getApprovalStatus());
        dto.setApprovalStatusDesc(summary.getApprovalStatusDesc());
        dto.setRiskLevel(summary.getRiskLevel());
        dto.setRiskLevelDesc(summary.getRiskLevelDesc());
        dto.setTriggerType(summary.getTriggerType());
        dto.setTriggerTypeDesc(summary.getTriggerTypeDesc());
        dto.setTriggerUser(summary.getTriggerUser());
        dto.setCurrentStep(summary.getCurrentStep());
        dto.setDuration(summary.getDuration());
        dto.setServerNames(summary.getServerNames());
        dto.setStartTime(summary.getStartTime());
        dto.setEndTime(summary.getEndTime());
        dto.setCreateTime(summary.getCreateTime());
        dto.setUpdateTime(summary.getUpdateTime());

        dto.setDeployWindow(record.getDeployWindow());
        dto.setWindowValid(record.getWindowValid());
        dto.setRequireApproval(ApprovalStatus.NOT_REQUIRED.getCode().equals(record.getApprovalStatus()) ? Boolean.FALSE : Boolean.TRUE);
        dto.setAutoRollback(record.getAutoRollback());
        dto.setGrayPercent(parseGrayPercent(record.getGrayConfig()));
        dto.setGrayInterval(parseGrayInterval(record.getGrayConfig()));
        dto.setChangeTicket(record.getChangeTicket());
        dto.setErrorMessage(record.getErrorMessage());
        dto.setSummary(record.getSummary());
        dto.setLogContent(record.getLogContent());
        dto.setRollbackFromDeployId(record.getRollbackFromDeployId());
        dto.setServers(parseServers(record.getServerSnapshot()));
        return dto;
    }

    private List<DeployStepDTO> listSteps(Long deployId) {
        LambdaQueryWrapper<DeployStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeployStep::getDeployId, deployId).orderByAsc(DeployStep::getStepOrder);
        return deployStepRepository.selectList(wrapper).stream().map(step -> {
            DeployStepDTO dto = new DeployStepDTO();
            dto.setStepKey(step.getStepKey());
            dto.setStepName(step.getStepName());
            dto.setStatus(step.getStatus());
            dto.setDuration(step.getDuration());
            dto.setDetail(step.getDetail());
            return dto;
        }).toList();
    }

    private List<ApprovalRecordDTO> listApprovalRecords(Long deployId) {
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalRecord::getDeployId, deployId).orderByDesc(ApprovalRecord::getId);
        return approvalRecordRepository.selectList(wrapper).stream().map(item -> {
            ApprovalRecordDTO dto = new ApprovalRecordDTO();
            dto.setId(item.getId());
            dto.setStatus(item.getStatus());
            dto.setStatusDesc(ApprovalStatus.fromCode(item.getStatus()).getDescription());
            dto.setApprover(item.getApprover());
            dto.setComment(item.getComment());
            dto.setApproveTime(item.getApproveTime());
            dto.setCreateTime(item.getCreateTime());
            return dto;
        }).toList();
    }

    private List<DeployServerDTO> parseServers(String json) {
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<DeployServerDTO>>() {
            });
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private List<DeployCheckItemDTO> parseHealthChecks(String json) {
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<DeployCheckItemDTO>>() {
            });
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private List<Long> parseLongList(String csv) {
        if (!StringUtils.hasText(csv)) {
            return new ArrayList<>();
        }
        List<Long> values = new ArrayList<>();
        for (String item : csv.split(",")) {
            if (StringUtils.hasText(item)) {
                values.add(Long.parseLong(item.trim()));
            }
        }
        return values;
    }

    private DeployServerDTO toServerDTO(Server server, ProjectServer mapping) {
        DeployServerDTO dto = new DeployServerDTO();
        dto.setId(server.getId());
        dto.setName(server.getName());
        dto.setHostname(server.getHostname());
        dto.setEnv(server.getEnv());
        dto.setDeployPath(mapping == null ? server.getWorkDir() : mapping.getDeployPath());
        dto.setStatus(server.getStatus());
        dto.setStatusDesc(serverStatusDesc(server.getStatus()));
        return dto;
    }

    private List<DeployCheckItemDTO> buildCheckItems(Project project,
                                                     BuildRecord buildRecord,
                                                     String env,
                                                     String deployWindow,
                                                     boolean windowValid,
                                                     BranchPolicyDTO policy,
                                                     boolean hasRunning,
                                                     List<DeployServerDTO> servers) {
        List<DeployCheckItemDTO> checks = new ArrayList<>();
        checks.add(check("项目状态", project.getStatus() != null && project.getStatus() == 1 ? "PASS" : "FAIL",
                project.getStatus() != null && project.getStatus() == 1 ? "项目已启用" : "项目已禁用"));
        checks.add(check("构建版本", "PASS", "目标构建 #" + buildRecord.getBuildNumber() + " 已成功完成"));
        checks.add(check("服务器映射", servers.isEmpty() ? "FAIL" : "PASS",
                servers.isEmpty() ? "未找到目标服务器" : "匹配到 " + servers.size() + " 台服务器"));
        long offlineCount = servers.stream().filter(item -> item.getStatus() == null || item.getStatus() == 0).count();
        checks.add(check("服务器连通性", offlineCount > 0 ? "FAIL" : "PASS",
                offlineCount > 0 ? "存在离线服务器，请先修复" : "所有目标服务器状态正常"));
        if (StringUtils.hasText(deployWindow)) {
            checks.add(check("发布窗口", windowValid ? "PASS" : ("prod".equalsIgnoreCase(env) ? "FAIL" : "WARN"),
                    windowValid ? "命中发布窗口" : "当前时间不在发布窗口内"));
        }
        checks.add(check("分支策略", policy != null ? "PASS" : "WARN",
                policy != null ? "已匹配分支策略 " + policy.getBranchPattern() : "未匹配到明确分支策略，采用默认规则"));
        checks.add(check("并发锁", hasRunning ? "FAIL" : "PASS", hasRunning ? "当前环境已有运行中部署" : "当前无并发冲突"));
        return checks;
    }

    private List<String> buildWarnings(boolean approvalRequired,
                                       boolean windowValid,
                                       boolean hasRunning,
                                       RiskLevel riskLevel,
                                       String changeTicket) {
        List<String> warnings = new ArrayList<>();
        if (approvalRequired) {
            warnings.add("当前部署需要审批后方可执行");
        }
        if (!windowValid) {
            warnings.add("当前时间不在发布窗口内");
        }
        if (hasRunning) {
            warnings.add("存在同项目同环境运行中部署");
        }
        if (riskLevel == RiskLevel.HIGH) {
            warnings.add("当前为高风险发布，请确认回滚方案与通知策略");
        }
        if (!StringUtils.hasText(changeTicket)) {
            warnings.add("建议关联变更单号，方便审计追踪");
        }
        return warnings;
    }

    private RiskLevel evaluateRisk(String env, String strategy, Integer grayPercent, int serverCount) {
        int score = 0;
        if ("prod".equalsIgnoreCase(env)) {
            score += 2;
        } else if ("test".equalsIgnoreCase(env)) {
            score += 1;
        }
        String strategyKey = normalizeStrategy(strategy);
        if ("BLUE_GREEN".equals(strategyKey) || "ROLLBACK".equals(strategyKey)) {
            score += 2;
        } else if ("GRAY".equals(strategyKey)) {
            score += (grayPercent != null && grayPercent >= 50) ? 2 : 1;
        } else if ("ROLLING".equals(strategyKey)) {
            score += 1;
        }
        if (serverCount >= 3) {
            score += 1;
        }
        if (score >= 4) {
            return RiskLevel.HIGH;
        }
        if (score >= 2) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private boolean hasRunningDeploy(Long projectId, String env) {
        QueryWrapper<DeployRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("project_id", projectId)
                .eq("env", env)
                .in("status", List.of(
                        DeployStatus.WAIT_APPROVAL.getCode(),
                        DeployStatus.PENDING.getCode(),
                        DeployStatus.RUNNING.getCode()
                ));
        return deployRecordRepository.selectCount(wrapper) > 0;
    }

    private boolean isWindowValid(String window) {
        if (!StringUtils.hasText(window)) {
            return true;
        }
        String[] parts = window.split("-");
        if (parts.length != 2) {
            return true;
        }
        try {
            LocalTime start = LocalTime.parse(parts[0].trim());
            LocalTime end = LocalTime.parse(parts[1].trim());
            LocalTime now = LocalTime.now();
            return !now.isBefore(start) && !now.isAfter(end);
        } catch (DateTimeParseException ex) {
            return true;
        }
    }

    private List<DeployStep> buildDeploySteps(Long deployId, String strategy, boolean rollbackMode) {
        List<DeployStep> steps = new ArrayList<>();
        int stepOrder = 1;
        steps.add(step(deployId, "PRECHECK", "部署前检查", stepOrder++, "校验项目、构建、服务器与发布窗口"));
        steps.add(step(deployId, rollbackMode ? "ROLLBACK_PREPARE" : "DEPLOY", rollbackMode ? "回滚准备" : "发布执行", stepOrder++, rollbackMode ? "准备回滚稳定版本" : "推送目标版本到服务器"));
        if ("灰度发布".equals(strategy)) {
            steps.add(step(deployId, "GRAY", "灰度放量", stepOrder++, "按灰度批次逐步放量"));
        } else if ("蓝绿发布".equals(strategy)) {
            steps.add(step(deployId, "SWITCH", "流量切换", stepOrder++, "完成蓝绿切换"));
        }
        steps.add(step(deployId, "VERIFY", "健康检查", stepOrder++, "校验服务存活、端口与版本"));
        steps.add(step(deployId, rollbackMode ? "ROLLBACK_DONE" : "DONE", rollbackMode ? "回滚完成" : "发布完成", stepOrder, rollbackMode ? "回滚流程结束" : "部署流程结束"));
        return steps;
    }

    private DeployStep step(Long deployId, String key, String name, int order, String detail) {
        DeployStep step = new DeployStep();
        step.setDeployId(deployId);
        step.setStepKey(key);
        step.setStepName(name);
        step.setStepOrder(order);
        step.setDetail(detail);
        return step;
    }

    private DeployCheckItemDTO check(String name, String status, String message) {
        DeployCheckItemDTO dto = new DeployCheckItemDTO();
        dto.setName(name);
        dto.setStatus(status);
        dto.setMessage(message);
        return dto;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private String appendLog(String source, String line) {
        String prefix = LocalDateTime.now().toString();
        if (!StringUtils.hasText(source)) {
            return "[" + prefix + "] " + line;
        }
        return source + System.lineSeparator() + "[" + prefix + "] " + line;
    }

    private String displayStrategy(String strategy) {
        return switch (normalizeStrategy(strategy)) {
            case "ROLLING" -> "滚动发布";
            case "BLUE_GREEN" -> "蓝绿发布";
            case "GRAY" -> "灰度发布";
            case "ROLLBACK" -> "回滚发布";
            default -> "单机部署";
        };
    }

    private String normalizeStrategy(String strategy) {
        if (!StringUtils.hasText(strategy)) {
            return "SINGLE";
        }
        String key = strategy.trim().toUpperCase().replace("-", "_");
        return switch (key) {
            case "滚动发布" -> "ROLLING";
            case "蓝绿发布" -> "BLUE_GREEN";
            case "灰度发布" -> "GRAY";
            case "回滚发布" -> "ROLLBACK";
            case "SINGLE", "ROLLING", "BLUE_GREEN", "GRAY", "ROLLBACK" -> key;
            default -> "SINGLE";
        };
    }

    private String displayTriggerType(String triggerType) {
        if (!StringUtils.hasText(triggerType)) {
            return "手动";
        }
        return switch (triggerType.toUpperCase()) {
            case "AUTO" -> "自动触发";
            case "API" -> "接口触发";
            default -> "手动触发";
        };
    }

    private String serverStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "离线";
            case 2 -> "忙碌";
            default -> "在线";
        };
    }

    private Integer parseGrayPercent(String grayConfig) {
        Map<String, Object> config = parseGrayConfig(grayConfig);
        Object value = config.get("grayPercent");
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    private Integer parseGrayInterval(String grayConfig) {
        Map<String, Object> config = parseGrayConfig(grayConfig);
        Object value = config.get("grayInterval");
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    private Map<String, Object> parseGrayConfig(String grayConfig) {
        if (!StringUtils.hasText(grayConfig)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(grayConfig, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            return new HashMap<>();
        }
    }

    private String suffixComment(String comment) {
        return StringUtils.hasText(comment) ? "，备注：" + comment : "";
    }

    private static class PreviewContext {
        private Project project;
        private BuildRecord buildRecord;
        private DeployPreviewDTO preview;
        private boolean requireApproval;
        private String deployWindow;
        private Integer grayPercent;
        private Integer grayInterval;
        private String triggerType;
        private String changeTicket;
        private Boolean autoRollback;
        private String operator;
    }
}
