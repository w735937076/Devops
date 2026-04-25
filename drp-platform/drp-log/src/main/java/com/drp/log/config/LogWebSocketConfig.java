package com.drp.log.config;

import com.drp.log.websocket.AppLogHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class LogWebSocketConfig implements WebSocketConfigurer {

    private final AppLogHandler appLogHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(appLogHandler, "/ws/log/server/{serverId}")
                .setAllowedOrigins("*");
    }
}
