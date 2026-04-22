package com.drp.server.service;

import com.drp.common.dto.PageResponse;
import com.drp.server.dto.*;

import java.util.List;

public interface ServerService {

    PageResponse<ServerDTO> list(ServerQueryRequest request);

    ServerDTO getById(Integer id);

    ServerDTO create(ServerCreateRequest request);

    ServerDTO update(Integer id, ServerUpdateRequest request);

    void delete(Integer id);

    ConnectionTestResult testConnection(Integer id);

    void updateHeartbeat(Integer id);

    List<ServerDTO> listAll();
}
