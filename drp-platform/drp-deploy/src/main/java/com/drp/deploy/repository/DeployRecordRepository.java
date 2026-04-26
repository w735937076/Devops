package com.drp.deploy.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.deploy.entity.DeployRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeployRecordRepository extends BaseMapper<DeployRecord> {
}
