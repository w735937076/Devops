package com.drp.server.controller;

import com.drp.common.dto.PageResponse;
import com.drp.common.result.Result;
import com.drp.server.dto.*;
import com.drp.server.service.ServerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
public class ServerController {

    private final ServerService serverService;

    @GetMapping
    public Result<PageResponse<ServerDTO>> list(ServerQueryRequest request) {
        return Result.success(serverService.list(request));
    }

    @GetMapping("/all")
    public Result<List<ServerDTO>> listAll() {
        return Result.success(serverService.listAll());
    }

    @GetMapping("/{id}")
    public Result<ServerDTO> getById(@PathVariable Integer id) {
        return Result.success(serverService.getById(id));
    }

    @PostMapping
    public Result<ServerDTO> create(@Valid @RequestBody ServerCreateRequest request) {
        return Result.success(serverService.create(request));
    }

    @PutMapping("/{id}")
    public Result<ServerDTO> update(@PathVariable Integer id, @Valid @RequestBody ServerUpdateRequest request) {
        return Result.success(serverService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        serverService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/test")
    public Result<ConnectionTestResult> testConnection(@PathVariable Integer id) {
        return Result.success(serverService.testConnection(id));
    }
}
