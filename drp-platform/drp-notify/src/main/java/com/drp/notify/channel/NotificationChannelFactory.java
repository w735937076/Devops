package com.drp.notify.channel;

import com.drp.notify.enums.NotificationChannelType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificationChannelFactory {

    private final Map<NotificationChannelType, NotificationChannel> channelMap;

    public NotificationChannelFactory(List<NotificationChannel> channels) {
        this.channelMap = channels.stream()
                .collect(Collectors.toMap(NotificationChannel::getChannelType, Function.identity()));
    }

    public NotificationChannel getChannel(NotificationChannelType type) {
        return channelMap.get(type);
    }

    public NotificationChannel getChannel(String type) {
        return channelMap.get(NotificationChannelType.valueOf(type));
    }
}
