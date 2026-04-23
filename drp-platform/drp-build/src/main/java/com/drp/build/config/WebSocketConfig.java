package com.drp.build.config;

import com.drp.build.websocket.BuildLogWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final BuildLogWebSocketHandler buildLogWebSocketHandler;

    public WebSocketConfig(BuildLogWebSocketHandler buildLogWebSocketHandler) {
        this.buildLogWebSocketHandler = buildLogWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(buildLogWebSocketHandler, "/ws/build/{id}/log")
                .setAllowedOrigins("*");
    }
}
