package com.drp.log.service;

import com.drp.common.dto.PageResponse;
import com.drp.log.dto.LogQueryRequest;
import com.drp.log.dto.OperationLogCreateRequest;
import com.drp.log.dto.OperationLogDTO;

public interface OperationLogService {

    PageResponse<OperationLogDTO> list(LogQueryRequest request);

    OperationLogDTO getById(Long id);

    void create(OperationLogCreateRequest request);

    String export(LogQueryRequest request);
}
