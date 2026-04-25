package com.drp.log.controller;

import com.drp.common.dto.PageResponse;
import com.drp.common.result.Result;
import com.drp.log.dto.LogQueryRequest;
import com.drp.log.dto.OperationLogDTO;
import com.drp.log.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/logs/operations")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResponse<OperationLogDTO>> list(
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        LogQueryRequest request = new LogQueryRequest();
        request.setOperatorId(operatorId);
        request.setProjectId(projectId);
        request.setOperationType(operationType);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setKeyword(keyword);
        request.setPage(page);
        request.setSize(size);

        PageResponse<OperationLogDTO> result = operationLogService.list(request);
        return Result.pageSuccess(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<OperationLogDTO> getById(@PathVariable Long id) {
        OperationLogDTO dto = operationLogService.getById(id);
        if (dto == null) {
            return Result.error("日志记录不存在");
        }
        return Result.success("操作成功", dto);
    }

    @GetMapping("/export")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> export(
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String keyword) {

        LogQueryRequest request = new LogQueryRequest();
        request.setOperatorId(operatorId);
        request.setProjectId(projectId);
        request.setOperationType(operationType);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setKeyword(keyword);

        String csv = operationLogService.export(request);

        String filename = "operation_logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
