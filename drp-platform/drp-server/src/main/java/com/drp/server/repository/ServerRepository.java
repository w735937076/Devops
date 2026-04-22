package com.drp.server.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.server.entity.Server;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ServerRepository extends BaseMapper<Server> {

    List<Server> findByStatus(Integer status);

    List<Server> findByGroupsContaining(String groupCode);
}
