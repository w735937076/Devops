package com.drp.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drp.common.dto.PageResponse;
import com.drp.log.dto.LogQueryRequest;
import com.drp.log.dto.OperationLogCreateRequest;
import com.drp.log.dto.OperationLogDTO;
import com.drp.log.entity.OperationLog;
import com.drp.log.repository.OperationLogRepository;
import com.drp.log.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogRepository operationLogRepository;

    @Override
    public PageResponse<OperationLogDTO> list(LogQueryRequest request) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (request.getOperatorId() != null) {
            wrapper.eq(OperationLog::getOperatorId, request.getOperatorId());
        }
        if (request.getProjectId() != null) {
            wrapper.eq(OperationLog::getProjectId, request.getProjectId());
        }
        if (StringUtils.hasText(request.getOperationType())) {
            wrapper.eq(OperationLog::getOperationType, request.getOperationType());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(OperationLog::getCreateTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(OperationLog::getCreateTime, request.getEndTime());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(OperationLog::getOperator, request.getKeyword())
                    .or()
                    .like(OperationLog::getProjectName, request.getKeyword())
                    .or()
                    .like(OperationLog::getDetail, request.getKeyword()));
        }

        wrapper.orderByDesc(OperationLog::getCreateTime);

        Page<OperationLog> page = new Page<>(request.getPage(), request.getSize());
        Page<OperationLog> result = operationLogRepository.selectPage(page, wrapper);

        List<OperationLogDTO> records = result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResponse.of(records, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    public OperationLogDTO getById(Long id) {
        OperationLog log = operationLogRepository.selectById(id);
        if (log == null) {
            return null;
        }
        return toDTO(log);
    }

    @Override
    @Transactional
    public void create(OperationLogCreateRequest request) {
        OperationLog operationLog = new OperationLog();
        BeanUtils.copyProperties(request, operationLog);
        operationLogRepository.insert(operationLog);
        log.debug("操作日志已创建: {}", operationLog.getId());
    }

    @Override
    public String export(LogQueryRequest request) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (request.getOperatorId() != null) {
            wrapper.eq(OperationLog::getOperatorId, request.getOperatorId());
        }
        if (request.getProjectId() != null) {
            wrapper.eq(OperationLog::getProjectId, request.getProjectId());
        }
        if (StringUtils.hasText(request.getOperationType())) {
            wrapper.eq(OperationLog::getOperationType, request.getOperationType());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(OperationLog::getCreateTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(OperationLog::getCreateTime, request.getEndTime());
        }

        wrapper.orderByDesc(OperationLog::getCreateTime);
        wrapper.last("LIMIT 10000");

        List<OperationLog> logs = operationLogRepository.selectList(wrapper);

        StringBuilder csv = new StringBuilder();
        csv.append("时间,操作人,操作类型,项目,环境,版本,状态,耗时,IP地址,详情,错误信息\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (OperationLog log : logs) {
            csv.append(log.getCreateTime() != null ? log.getCreateTime().format(formatter) : "").append(",");
            csv.append(escapeCsv(log.getOperator())).append(",");
            csv.append(escapeCsv(log.getOperationType())).append(",");
            csv.append(escapeCsv(log.getProjectName())).append(",");
            csv.append(escapeCsv(log.getEnv())).append(",");
            csv.append(escapeCsv(log.getVersion())).append(",");
            csv.append(escapeCsv(log.getStatus())).append(",");
            csv.append(log.getDuration() != null ? log.getDuration() : "").append(",");
            csv.append(escapeCsv(log.getIp())).append(",");
            csv.append(escapeCsv(log.getDetail())).append(",");
            csv.append(escapeCsv(log.getErrorMessage())).append("\n");
        }

        return csv.toString();
    }

    private OperationLogDTO toDTO(OperationLog entity) {
        OperationLogDTO dto = new OperationLogDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
