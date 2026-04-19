package com.drp.project.service;

import com.drp.common.dto.PageResponse;
import com.drp.project.dto.CredentialCreateRequest;
import com.drp.project.dto.CredentialDTO;
import com.drp.project.dto.CredentialQueryRequest;
import com.drp.project.dto.CredentialUpdateRequest;

/**
 * 凭证管理服务
 *
 * @author Nick
 */
public interface CredentialService {

    PageResponse<CredentialDTO> queryPage(CredentialQueryRequest request);

    CredentialDTO getById(Long id);

    CredentialDTO create(CredentialCreateRequest request);

    CredentialDTO update(Long id, CredentialUpdateRequest request);

    void delete(Long id);

    String getDecryptedContent(Long id);
}
