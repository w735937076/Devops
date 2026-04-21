package com.drp.project.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.project.entity.EnvVariable;
import org.apache.ibatis.annotations.Mapper;

/**
 * 环境变量仓储
 *
 * @author Nick
 */
@Mapper
public interface EnvVariableRepository extends BaseMapper<EnvVariable> {
}