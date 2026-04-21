package com.drp.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drp.common.exception.BusinessException;
import com.drp.project.dto.BranchPolicyCreateRequest;
import com.drp.project.dto.BranchPolicyDTO;
import com.drp.project.dto.BranchPolicyUpdateRequest;
import com.drp.project.entity.BranchPolicy;
import com.drp.project.repository.BranchPolicyRepository;
import com.drp.project.service.BranchPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 分支策略服务实现
 *
 * @author Nick
 */
@Service
public class BranchPolicyServiceImpl implements BranchPolicyService {

    private final BranchPolicyRepository branchPolicyRepository;

    public BranchPolicyServiceImpl(BranchPolicyRepository branchPolicyRepository) {
        this.branchPolicyRepository = branchPolicyRepository;
    }

    @Override
    public List<BranchPolicyDTO> listByProjectId(Long projectId) {
        LambdaQueryWrapper<BranchPolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BranchPolicy::getProjectId, projectId);
        wrapper.orderByDesc(BranchPolicy::getCreateTime);

        List<BranchPolicy> policies = branchPolicyRepository.selectList(wrapper);
        return policies.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BranchPolicyDTO create(BranchPolicyCreateRequest request) {
        BranchPolicy policy = new BranchPolicy();
        policy.setProjectId(request.getProjectId());
        policy.setBranchPattern(request.getBranchPattern());
        policy.setAllowAutoDeploy(request.getAllowAutoDeploy() != null ? request.getAllowAutoDeploy() : 0);
        policy.setRequireApproval(request.getRequireApproval() != null ? request.getRequireApproval() : 0);
        policy.setCreateTime(LocalDateTime.now());

        branchPolicyRepository.insert(policy);
        return toDTO(policy);
    }

    @Override
    @Transactional
    public BranchPolicyDTO update(Long id, BranchPolicyUpdateRequest request) {
        BranchPolicy policy = branchPolicyRepository.selectById(id);
        if (policy == null) {
            throw new BusinessException("分支策略不存在");
        }

        policy.setBranchPattern(request.getBranchPattern());
        policy.setAllowAutoDeploy(request.getAllowAutoDeploy());
        policy.setRequireApproval(request.getRequireApproval());

        branchPolicyRepository.updateById(policy);
        return toDTO(policy);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        branchPolicyRepository.deleteById(id);
    }

    @Override
    public long countByProjectId(Long projectId) {
        LambdaQueryWrapper<BranchPolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BranchPolicy::getProjectId, projectId);
        return branchPolicyRepository.selectCount(wrapper);
    }

    @Override
    public BranchPolicyDTO matchPolicy(Long projectId, String branchName) {
        List<BranchPolicyDTO> policies = listByProjectId(projectId);

        for (BranchPolicyDTO policy : policies) {
            if (matches(branchName, policy.getBranchPattern())) {
                return policy;
            }
        }
        return null;
    }

    /**
     * 判断分支名称是否匹配策略模式
     * 支持通配符 * 匹配任意字符
     */
    private boolean matches(String branchName, String pattern) {
        if (!StringUtils.hasText(pattern)) {
            return false;
        }
        // 将通配符转换为正则表达式
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*");
        return Pattern.matches(regex, branchName);
    }

    private BranchPolicyDTO toDTO(BranchPolicy policy) {
        BranchPolicyDTO dto = new BranchPolicyDTO();
        dto.setId(policy.getId());
        dto.setProjectId(policy.getProjectId());
        dto.setBranchPattern(policy.getBranchPattern());
        dto.setAllowAutoDeploy(policy.getAllowAutoDeploy());
        dto.setAllowAutoDeployDesc(policy.getAllowAutoDeploy() == 1 ? "是" : "否");
        dto.setRequireApproval(policy.getRequireApproval());
        dto.setRequireApprovalDesc(policy.getRequireApproval() == 1 ? "是" : "否");
        dto.setCreateTime(policy.getCreateTime());
        return dto;
    }
}