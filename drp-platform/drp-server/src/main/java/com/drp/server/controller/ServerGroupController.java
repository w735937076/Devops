package com.drp.server.controller;

import com.drp.common.result.Result;
import com.drp.server.dto.ServerGroupCreateRequest;
import com.drp.server.dto.ServerGroupDTO;
import com.drp.server.dto.ServerGroupUpdateRequest;
import com.drp.server.service.ServerGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/server-groups")
@RequiredArgsConstructor
public class ServerGroupController {

    private final ServerGroupService serverGroupService;

    @GetMapping
    public Result<List<ServerGroupDTO>> list() {
        return Result.success(serverGroupService.list());
    }

    @GetMapping("/{id}")
    public Result<ServerGroupDTO> getById(@PathVariable Long id) {
        return Result.success(serverGroupService.getById(id));
    }

    @PostMapping
    public Result<ServerGroupDTO> create(@Valid @RequestBody ServerGroupCreateRequest request) {
        return Result.success(serverGroupService.create(request));
    }

    @PutMapping("/{id}")
    public Result<ServerGroupDTO> update(@PathVariable Long id, @Valid @RequestBody ServerGroupUpdateRequest request) {
        return Result.success(serverGroupService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        serverGroupService.delete(id);
        return Result.success();
    }
}
