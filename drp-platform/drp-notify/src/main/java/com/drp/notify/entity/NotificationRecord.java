package com.drp.notify.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ntf_notification_record")
public class NotificationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long configId;

    private String channel;

    private String event;

    private String recipient;

    private String title;

    private String content;

    private String status;

    private Integer retryCount;

    private Integer maxRetry;

    private String errorMessage;

    private LocalDateTime sendTime;

    private LocalDateTime sentTime;

    private LocalDateTime readTime;

    private String externalId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
