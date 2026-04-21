package com.drp.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drp.common.exception.BusinessException;
import com.drp.project.dto.EnvVariableCreateRequest;
import com.drp.project.dto.EnvVariableDTO;
import com.drp.project.dto.EnvVariableUpdateRequest;
import com.drp.project.entity.EnvVariable;
import com.drp.project.repository.EnvVariableRepository;
import com.drp.project.service.EnvVariableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 环境变量服务实现
 *
 * @author Nick
 */
@Service
public class EnvVariableServiceImpl implements EnvVariableService {

    private final EnvVariableRepository envVariableRepository;

    public EnvVariableServiceImpl(EnvVariableRepository envVariableRepository) {
        this.envVariableRepository = envVariableRepository;
    }

    @Override
    public List<EnvVariableDTO> listByProjectId(Long projectId, String envCode) {
        LambdaQueryWrapper<EnvVariable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvVariable::getProjectId, projectId);
        if (StringUtils.hasText(envCode)) {
            wrapper.eq(EnvVariable::getEnvCode, envCode);
        }
        wrapper.orderByAsc(EnvVariable::getEnvCode, EnvVariable::getVarKey);

        List<EnvVariable> variables = envVariableRepository.selectList(wrapper);
        return variables.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EnvVariableDTO create(EnvVariableCreateRequest request) {
        EnvVariable variable = new EnvVariable();
        variable.setProjectId(request.getProjectId());
        variable.setEnvCode(request.getEnvCode());
        variable.setVarKey(request.getVarKey());
        variable.setVarValue(request.getVarValue());
        variable.setIsSecret(request.getIsSecret() != null ? request.getIsSecret() : 0);
        variable.setCreateTime(LocalDateTime.now());

        envVariableRepository.insert(variable);
        return toDTO(variable);
    }

    @Override
    @Transactional
    public EnvVariableDTO update(Long id, EnvVariableUpdateRequest request) {
        EnvVariable variable = envVariableRepository.selectById(id);
        if (variable == null) {
            throw new BusinessException("环境变量不存在");
        }

        variable.setEnvCode(request.getEnvCode());
        variable.setVarKey(request.getVarKey());
        variable.setVarValue(request.getVarValue());
        variable.setIsSecret(request.getIsSecret());

        envVariableRepository.updateById(variable);
        return toDTO(variable);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        envVariableRepository.deleteById(id);
    }

    @Override
    public long countByProjectId(Long projectId) {
        LambdaQueryWrapper<EnvVariable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvVariable::getProjectId, projectId);
        return envVariableRepository.selectCount(wrapper);
    }

    private EnvVariableDTO toDTO(EnvVariable variable) {
        EnvVariableDTO dto = new EnvVariableDTO();
        dto.setId(variable.getId());
        dto.setProjectId(variable.getProjectId());
        dto.setEnvCode(variable.getEnvCode());
        // 返回真实值，前端根据 isSecret 自行决定显示方式
        dto.setVarKey(variable.getVarKey());
        dto.setVarValue(variable.getVarValue());
        dto.setIsSecret(variable.getIsSecret());
        dto.setIsSecretDesc(variable.getIsSecret() == 1 ? "是" : "否");
        dto.setCreateTime(variable.getCreateTime());

        // 环境名称
        dto.setEnvName(getEnvName(variable.getEnvCode()));

        return dto;
    }

    private String getEnvName(String envCode) {
        return switch (envCode) {
            case "dev" -> "开发环境";
            case "test" -> "测试环境";
            case "prod" -> "生产环境";
            default -> envCode;
        };
    }
}