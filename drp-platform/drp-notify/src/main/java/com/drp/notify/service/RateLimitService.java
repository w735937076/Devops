package com.drp.notify.service;

import com.drp.notify.dto.NotifyEvent;

public interface RateLimitService {
    boolean shouldSend(NotifyEvent event, String channel, String recipient);
    void recordSend(NotifyEvent event, String channel, String recipient);
}
