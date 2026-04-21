package com.drp.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drp.common.exception.BusinessException;
import com.drp.project.client.UserServiceClient;
import com.drp.project.dto.MemberAddRequest;
import com.drp.project.dto.MemberUpdateRequest;
import com.drp.project.dto.ProjectMemberDTO;
import com.drp.project.entity.Project;
import com.drp.project.entity.ProjectMember;
import com.drp.project.enums.MemberRole;
import com.drp.project.repository.ProjectMemberRepository;
import com.drp.project.repository.ProjectRepository;
import com.drp.project.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目成员管理服务实现
 *
 * @author Nick
 */
@Service
public class MemberServiceImpl implements MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberServiceImpl.class);

    private final ProjectMemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final UserServiceClient userServiceClient;

    public MemberServiceImpl(ProjectMemberRepository memberRepository, ProjectRepository projectRepository, UserServiceClient userServiceClient) {
        this.memberRepository = memberRepository;
        this.projectRepository = projectRepository;
        this.userServiceClient = userServiceClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberDTO> getMembers(Long projectId) {
        requireProject(projectId);

        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
                .orderByDesc(ProjectMember::getId);

        List<ProjectMember> members = memberRepository.selectList(wrapper);
        return members.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProjectMemberDTO addMember(Long projectId, MemberAddRequest request) {
        requireProject(projectId);
        validateRole(request.getRole());

        // 校验用户是否已是项目成员
        if (memberRepository.countByProjectIdAndUserId(projectId, request.getUserId()) > 0) {
            throw new BusinessException("该用户已是项目成员");
        }

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(request.getUserId());
        member.setRole(request.getRole());

        memberRepository.insert(member);
        log.info("添加项目成员成功 | projectId: {} | userId: {} | role: {}", projectId, request.getUserId(), request.getRole());

        return toDTO(memberRepository.selectById(member.getId()));
    }

    @Override
    @Transactional
    public ProjectMemberDTO updateMember(Long projectId, Long userId, MemberUpdateRequest request) {
        requireProject(projectId);
        validateRole(request.getRole());

        ProjectMember member = requireMember(projectId, userId);

        member.setRole(request.getRole());
        memberRepository.updateById(member);
        log.info("更新项目成员角色成功 | projectId: {} | userId: {} | role: {}", projectId, userId, request.getRole());

        return toDTO(member);
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        requireProject(projectId);

        ProjectMember member = requireMember(projectId, userId);

        // 不能移除项目所有者
        if (MemberRole.OWNER.name().equals(member.getRole())) {
            throw new BusinessException("不能移除项目所有者");
        }

        memberRepository.deleteById(member.getId());
        log.info("移除项目成员成功 | projectId: {} | userId: {}", projectId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProjectOwner(Long projectId, Long userId) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId)
                .eq(ProjectMember::getRole, MemberRole.OWNER.name());

        return memberRepository.selectCount(wrapper) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasWritePermission(Long projectId, Long userId) {
        // OWNER 和 DEVELOPER 都有写权限
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId)
                .in(ProjectMember::getRole, MemberRole.OWNER.name(), MemberRole.DEVELOPER.name());

        return memberRepository.selectCount(wrapper) > 0;
    }

    private Project requireProject(Long id) {
        Project project = projectRepository.selectById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return project;
    }

    private ProjectMember requireMember(Long projectId, Long userId) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId);

        ProjectMember member = memberRepository.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException("项目成员不存在");
        }
        return member;
    }

    private void validateRole(String role) {
        if (!MemberRole.isValid(role)) {
            throw new BusinessException("不支持的角色: " + role);
        }
    }

    private ProjectMemberDTO toDTO(ProjectMember member) {
        ProjectMemberDTO dto = new ProjectMemberDTO();
        dto.setId(member.getId());
        dto.setProjectId(member.getProjectId());
        dto.setUserId(member.getUserId());
        dto.setRole(member.getRole());
        dto.setCreateTime(member.getCreateTime());

        // 填充角色描述和权限说明
        MemberRole memberRole = MemberRole.valueOf(member.getRole());
        dto.setRoleDesc(memberRole.getDescription());
        dto.setPermission(memberRole.getPermission());

        // 通过用户服务获取真实用户名和姓名
        UserServiceClient.UserInfo userInfo = userServiceClient.getUserById(member.getUserId());
        dto.setUsername(userInfo.getUsername());
        dto.setRealName(userInfo.getRealName());

        return dto;
    }

    /**
     * 获取当前登录用户ID
     */
    protected Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                return (Long) principal;
            }
        }
        return null;
    }
}