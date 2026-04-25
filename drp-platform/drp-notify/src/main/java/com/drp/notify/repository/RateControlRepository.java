package com.drp.notify.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drp.notify.entity.RateControl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface RateControlRepository extends BaseMapper<RateControl> {

    @Select("SELECT * FROM ntf_rate_control WHERE event = #{event} AND channel = #{channel} AND recipient = #{recipient} AND project_id = #{projectId}")
    Optional<RateControl> findByEventAndChannelAndRecipient(@Param("event") String event,
                                                             @Param("channel") String channel,
                                                             @Param("recipient") String recipient,
                                                             @Param("projectId") Long projectId);
}
