package com.drp.server.service;

import com.drp.server.dto.ServerGroupCreateRequest;
import com.drp.server.dto.ServerGroupDTO;
import com.drp.server.dto.ServerGroupUpdateRequest;

import java.util.List;

public interface ServerGroupService {

    List<ServerGroupDTO> list();

    ServerGroupDTO getById(Long id);

    ServerGroupDTO create(ServerGroupCreateRequest request);

    ServerGroupDTO update(Long id, ServerGroupUpdateRequest request);

    void delete(Long id);
}
