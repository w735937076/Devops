package com.drp.build.controller;

import com.drp.build.dto.BuildDetailDTO;
import com.drp.build.dto.BuildTriggerRequest;
import com.drp.build.service.BuildService;
import com.drp.common.dto.PageResponse;
import com.drp.common.result.Result;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/builds")
public class BuildController {

    private static final Logger log = LoggerFactory.getLogger(BuildController.class);

    private final BuildService buildService;

    public BuildController(BuildService buildService) {
        this.buildService = buildService;
    }

    /**
     * 触发构建
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<BuildDetailDTO> triggerBuild(
            @Valid @RequestBody BuildTriggerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("触发构建请求 | projectId: {} | branch: {} | user: {}",
                request.getProjectId(), request.getBranch(), userDetails.getUsername());

        BuildDetailDTO result = buildService.triggerBuild(request, userDetails.getUsername());
        return Result.success("构建已触发", result);
    }

    /**
     * 获取构建列表
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResponse<BuildDetailDTO>> queryPage(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String branch,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return Result.pageSuccess(buildService.queryPage(projectId, status, branch, page, size));
    }

    /**
     * 获取构建详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<BuildDetailDTO> getDetail(@PathVariable Long id) {
        return Result.success(buildService.getBuildDetail(id));
    }

    /**
     * 取消构建
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancelBuild(@PathVariable Long id) {
        log.info("取消构建请求 | buildId: {}", id);
        buildService.cancelBuild(id);
        return Result.success("构建已取消");
    }

    /**
     * 获取构建日志
     */
    @GetMapping("/{id}/log")
    @PreAuthorize("isAuthenticated()")
    public Result<String> getBuildLog(@PathVariable Long id) {
        String log = buildService.getBuildLog(id);
        return Result.success("操作成功", log);
    }

    /**
     * 获取构建产物
     */
    @GetMapping("/{id}/artifact")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getArtifact(@PathVariable Long id) {
        BuildDetailDTO detail = buildService.getBuildDetail(id);
        return Result.success("操作成功", detail.getArtifacts());
    }

    /**
     * 下载构建产物
     */
    @GetMapping("/{id}/artifact/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadArtifact(
            @PathVariable Long id,
            @RequestParam String path) {

        BuildDetailDTO detail = buildService.getBuildDetail(id);
        boolean valid = detail.getArtifacts() != null &&
                detail.getArtifacts().stream().anyMatch(a -> path.equals(a.getPath()));
        if (!valid) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        String encodedName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .contentLength(file.length())
                .body(new FileSystemResource(file));
    }
}
