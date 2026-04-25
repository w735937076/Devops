package com.drp.notify.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ntf_rate_control")
public class RateControl {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String event;

    private String channel;

    private String recipient;

    private LocalDateTime lastSendTime;

    private Integer sendCount;

    private LocalDateTime windowStart;
}
