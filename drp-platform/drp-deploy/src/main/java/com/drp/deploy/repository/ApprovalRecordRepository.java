package com.drp.deploy.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.deploy.entity.ApprovalRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApprovalRecordRepository extends BaseMapper<ApprovalRecord> {
}
