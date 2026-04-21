package com.drp.project.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.project.entity.BranchPolicy;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分支策略仓储
 *
 * @author Nick
 */
@Mapper
public interface BranchPolicyRepository extends BaseMapper<BranchPolicy> {
}