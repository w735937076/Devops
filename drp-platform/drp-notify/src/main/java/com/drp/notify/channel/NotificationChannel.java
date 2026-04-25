package com.drp.notify.channel;

import com.drp.notify.dto.NotifyMessage;
import com.drp.notify.dto.SendResult;
import com.drp.notify.enums.NotificationChannelType;

public interface NotificationChannel {

    NotificationChannelType getChannelType();

    String getChannelName();

    SendResult send(NotifyMessage message, String config);

    boolean test(String config);

    String getConfigSchema();
}
