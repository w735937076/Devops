package com.drp.project.controller;

import com.drp.common.result.Result;
import com.drp.project.dto.MemberAddRequest;
import com.drp.project.dto.MemberUpdateRequest;
import com.drp.project.dto.ProjectMemberDTO;
import com.drp.project.service.MemberService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目成员管理控制器
 *
 * @author Nick
 */
@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private static final Logger log = LoggerFactory.getLogger(ProjectMemberController.class);

    private final MemberService memberService;

    public ProjectMemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 获取项目成员列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<List<ProjectMemberDTO>> getMembers(@PathVariable Long projectId) {
        return Result.success(memberService.getMembers(projectId));
    }

    /**
     * 添加项目成员
     */
    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<ProjectMemberDTO> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody MemberAddRequest request) {
        log.info("添加项目成员 | projectId: {} | userId: {} | role: {}", projectId, request.getUserId(), request.getRole());
        return Result.success("添加成功", memberService.addMember(projectId, request));
    }

    /**
     * 更新成员角色
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<ProjectMemberDTO> updateMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody MemberUpdateRequest request) {
        log.info("更新项目成员角色 | projectId: {} | userId: {} | role: {}", projectId, userId, request.getRole());
        return Result.success("更新成功", memberService.updateMember(projectId, userId, request));
    }

    /**
     * 移除项目成员
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGE')")
    public Result<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        log.info("移除项目成员 | projectId: {} | userId: {}", projectId, userId);
        memberService.removeMember(projectId, userId);
        return Result.success("移除成功");
    }
}