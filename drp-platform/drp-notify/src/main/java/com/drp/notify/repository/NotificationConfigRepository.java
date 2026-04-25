package com.drp.notify.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.notify.entity.NotificationConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NotificationConfigRepository extends BaseMapper<NotificationConfig> {

    List<NotificationConfig> findByProjectIdAndEventAndEnabled(Long projectId, String event, Boolean enabled);

    List<NotificationConfig> findByProjectIdIsNullAndEventAndEnabled(String event, Boolean enabled);
}
