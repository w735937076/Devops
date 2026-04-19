package com.drp.project.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.project.entity.Credential;
import org.apache.ibatis.annotations.Mapper;

/**
 * 凭证仓储
 *
 * @author Nick
 */
@Mapper
public interface CredentialRepository extends BaseMapper<Credential> {
}
