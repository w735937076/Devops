package com.drp.notify.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drp.notify.entity.NotificationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface NotificationRecordRepository extends BaseMapper<NotificationRecord> {

    @Select("SELECT * FROM ntf_notification_record WHERE config_id = #{configId} AND recipient = #{recipient} ORDER BY create_time DESC LIMIT 1")
    Optional<NotificationRecord> findLatestByConfigIdAndRecipient(@Param("configId") Long configId, @Param("recipient") String recipient);

    IPage<NotificationRecord> selectPageByChannelAndStatus(Page<NotificationRecord> page,
                                                           @Param("channel") String channel,
                                                           @Param("status") String status,
                                                           @Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);
}
