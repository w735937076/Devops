package com.drp.notify.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ntf_template")
public class NotificationTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String channel;

    private String event;

    private String titleTemplate;

    private String contentTemplate;

    private String variables;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
