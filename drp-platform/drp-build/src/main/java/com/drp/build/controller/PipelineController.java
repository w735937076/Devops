package com.drp.build.controller;

import com.drp.build.entity.Pipeline;
import com.drp.build.repository.PipelineRepository;
import com.drp.build.service.PipelineService;
import com.drp.common.result.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/pipeline")
public class PipelineController {

    private final PipelineRepository pipelineRepository;
    private final PipelineService pipelineService;

    public PipelineController(PipelineRepository pipelineRepository, PipelineService pipelineService) {
        this.pipelineRepository = pipelineRepository;
        this.pipelineService = pipelineService;
    }

    /**
     * 获取项目流水线列表
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<Pipeline>> getPipelineList(@PathVariable Long projectId) {
        return Result.success(pipelineService.getPipelineList(projectId));
    }

    /**
     * 获取流水线详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Pipeline> getPipelineDetail(@PathVariable Long projectId, @PathVariable Long id) {
        return Result.success(pipelineService.getById(id));
    }

    /**
     * 创建流水线
     */
    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<Pipeline> createPipeline(@PathVariable Long projectId, @RequestBody Pipeline pipeline) {
        pipeline.setProjectId(projectId);
        Pipeline result = pipelineService.create(pipeline);
        return Result.success("创建成功", result);
    }

    /**
     * 更新流水线
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<Pipeline> updatePipeline(@PathVariable Long id, @RequestBody Pipeline pipeline) {
        pipeline.setId(id);
        Pipeline result = pipelineService.update(pipeline);
        return Result.success("更新成功", result);
    }

    /**
     * 删除流水线
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<Void> deletePipeline(@PathVariable Long id) {
        pipelineService.delete(id);
        return Result.success("删除成功");
    }
}
