package com.drp.notify.dto;

import lombok.Data;

@Data
public class ChannelConfigRequest {
    private String channel;
    private Object config;
}
