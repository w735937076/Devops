package com.drp.deploy.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.deploy.entity.DeployStep;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeployStepRepository extends BaseMapper<DeployStep> {
}
