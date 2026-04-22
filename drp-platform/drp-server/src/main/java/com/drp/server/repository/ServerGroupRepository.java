package com.drp.server.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.server.entity.ServerGroup;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ServerGroupRepository extends BaseMapper<ServerGroup> {
}
