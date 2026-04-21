package com.drp.project.service;

import com.drp.project.dto.MemberAddRequest;
import com.drp.project.dto.MemberUpdateRequest;
import com.drp.project.dto.ProjectMemberDTO;

import java.util.List;

/**
 * 项目成员管理服务
 *
 * @author Nick
 */
public interface MemberService {

    /**
     * 获取项目成员列表
     */
    List<ProjectMemberDTO> getMembers(Long projectId);

    /**
     * 添加项目成员
     */
    ProjectMemberDTO addMember(Long projectId, MemberAddRequest request);

    /**
     * 更新成员角色
     */
    ProjectMemberDTO updateMember(Long projectId, Long userId, MemberUpdateRequest request);

    /**
     * 移除项目成员
     */
    void removeMember(Long projectId, Long userId);

    /**
     * 校验用户是否为项目所有者
     */
    boolean isProjectOwner(Long projectId, Long userId);

    /**
     * 校验用户是否有项目写权限
     */
    boolean hasWritePermission(Long projectId, Long userId);
}