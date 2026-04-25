package com.drp.notify.service;

import com.drp.notify.dto.NotifyEvent;

public interface NotificationService {
    void sendNotification(NotifyEvent event);
}
