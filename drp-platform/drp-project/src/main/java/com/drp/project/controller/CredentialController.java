package com.drp.project.controller;

import com.drp.common.dto.PageResponse;
import com.drp.common.result.Result;
import com.drp.project.dto.CredentialCreateRequest;
import com.drp.project.dto.CredentialDTO;
import com.drp.project.dto.CredentialQueryRequest;
import com.drp.project.dto.CredentialUpdateRequest;
import com.drp.project.service.CredentialService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 凭证管理控制器
 *
 * @author Nick
 */
@RestController
@RequestMapping("/api/credentials")
public class CredentialController {

    private static final Logger log = LoggerFactory.getLogger(CredentialController.class);

    private final CredentialService credentialService;

    public CredentialController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<PageResponse<CredentialDTO>> queryPage(CredentialQueryRequest request) {
        return Result.pageSuccess(credentialService.queryPage(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<CredentialDTO> getById(@PathVariable("id") Long id) {
        return Result.success(credentialService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<CredentialDTO> create(@Valid @RequestBody CredentialCreateRequest request) {
        log.info("创建凭证 | name: {} | type: {}", request.getName(), request.getType());
        return Result.success("创建成功", credentialService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<CredentialDTO> update(@PathVariable("id") Long id, @Valid @RequestBody CredentialUpdateRequest request) {
        log.info("更新凭证 | id: {}", id);
        return Result.success("更新成功", credentialService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<Void> delete(@PathVariable("id") Long id) {
        log.info("删除凭证 | id: {}", id);
        credentialService.delete(id);
        return Result.success("删除成功");
    }
}
