package com.drp.notify.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.notify.entity.NotificationTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationTemplateRepository extends BaseMapper<NotificationTemplate> {
}
