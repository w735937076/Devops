package com.drp.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drp.common.dto.PageResponse;
import com.drp.common.exception.BusinessException;
import com.drp.common.util.AesEncryptUtil;
import com.drp.project.dto.CredentialCreateRequest;
import com.drp.project.dto.CredentialDTO;
import com.drp.project.dto.CredentialQueryRequest;
import com.drp.project.dto.CredentialUpdateRequest;
import com.drp.project.entity.Credential;
import com.drp.project.enums.CredentialType;
import com.drp.project.repository.CredentialRepository;
import com.drp.project.repository.ProjectRepository;
import com.drp.project.service.CredentialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 凭证管理服务实现
 *
 * @author Nick
 */
@Service
public class CredentialServiceImpl implements CredentialService {

    private static final Logger log = LoggerFactory.getLogger(CredentialServiceImpl.class);

    private final CredentialRepository credentialRepository;
    private final ProjectRepository projectRepository;

    public CredentialServiceImpl(CredentialRepository credentialRepository, ProjectRepository projectRepository) {
        this.credentialRepository = credentialRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CredentialDTO> queryPage(CredentialQueryRequest request) {
        request.validate();

        Page<Credential> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<Credential> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(q -> q.like(Credential::getName, request.getKeyword())
                    .or()
                    .like(Credential::getAccount, request.getKeyword())
                    .or()
                    .like(Credential::getDescription, request.getKeyword()));
        }
        if (StringUtils.hasText(request.getType())) {
            validateCredentialType(request.getType());
            wrapper.eq(Credential::getType, request.getType());
        }

        wrapper.orderByDesc(Credential::getId);

        Page<Credential> result = credentialRepository.selectPage(page, wrapper);
        List<CredentialDTO> records = result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResponse.of(records, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public CredentialDTO getById(Long id) {
        Credential credential = requireCredential(id);
        return toDTO(credential);
    }

    @Override
    @Transactional
    public CredentialDTO create(CredentialCreateRequest request) {
        validateCredentialType(request.getType());

        Credential credential = new Credential();
        credential.setName(request.getName());
        credential.setType(request.getType());
        credential.setAccount(request.getAccount());
        credential.setDescription(request.getDescription());
        credential.setSecretContent(AesEncryptUtil.encrypt(request.getSecretContent()));

        credentialRepository.insert(credential);
        log.info("创建凭证成功 | id: {} | name: {}", credential.getId(), credential.getName());
        return toDTO(credentialRepository.selectById(credential.getId()));
    }

    @Override
    @Transactional
    public CredentialDTO update(Long id, CredentialUpdateRequest request) {
        validateCredentialType(request.getType());

        Credential credential = requireCredential(id);
        credential.setName(request.getName());
        credential.setType(request.getType());
        credential.setAccount(request.getAccount());
        credential.setDescription(request.getDescription());
        if (StringUtils.hasText(request.getSecretContent())) {
            credential.setSecretContent(AesEncryptUtil.encrypt(request.getSecretContent()));
        }

        credentialRepository.updateById(credential);
        log.info("更新凭证成功 | id: {} | name: {}", credential.getId(), credential.getName());
        return toDTO(credentialRepository.selectById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireCredential(id);
        long referencedCount = projectRepository.countByCredentialId(id);
        if (referencedCount > 0) {
            throw new BusinessException("当前凭证已被项目引用，请先解除项目关联后再删除");
        }
        credentialRepository.deleteById(id);
        log.info("删除凭证成功 | id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public String getDecryptedContent(Long id) {
        Credential credential = requireCredential(id);
        return AesEncryptUtil.decrypt(credential.getSecretContent());
    }

    private Credential requireCredential(Long id) {
        Credential credential = credentialRepository.selectById(id);
        if (credential == null) {
            throw new BusinessException("凭证不存在");
        }
        return credential;
    }

    private void validateCredentialType(String type) {
        if (!CredentialType.isValid(type)) {
            throw new BusinessException("不支持的凭证类型: " + type);
        }
    }

    private CredentialDTO toDTO(Credential credential) {
        CredentialDTO dto = new CredentialDTO();
        dto.setId(credential.getId());
        dto.setName(credential.getName());
        dto.setType(credential.getType());
        dto.setTypeDesc(CredentialType.valueOf(credential.getType()).getDescription());
        dto.setAccount(credential.getAccount());
        dto.setDescription(credential.getDescription());
        dto.setSecretMasked(maskSecret(credential.getSecretContent()));
        dto.setReferencedProjectCount(projectRepository.countByCredentialId(credential.getId()));
        dto.setCreateTime(credential.getCreateTime());
        dto.setUpdateTime(credential.getUpdateTime());
        return dto;
    }

    private String maskSecret(String encryptedContent) {
        String decrypted = AesEncryptUtil.decrypt(encryptedContent);
        if (!StringUtils.hasText(decrypted)) {
            return "";
        }
        if (decrypted.length() <= 4) {
            return "*".repeat(decrypted.length());
        }
        return decrypted.substring(0, 2) + "*".repeat(Math.max(4, decrypted.length() - 4)) + decrypted.substring(decrypted.length() - 2);
    }
}
