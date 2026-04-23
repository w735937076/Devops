package com.drp.build.websocket;

import com.drp.build.service.BuildService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class BuildLogWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(BuildLogWebSocketHandler.class);

    private final BuildService buildService;

    public BuildLogWebSocketHandler(BuildService buildService) {
        this.buildService = buildService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long buildId = extractBuildId(session);
        if (buildId != null) {
            buildService.registerLogSession(buildId, session);
            log.info("WebSocket 连接建立 | buildId: {}", buildId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 客户端消息处理（可选，用于心跳等）
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long buildId = extractBuildId(session);
        if (buildId != null) {
            buildService.unregisterLogSession(buildId);
            log.info("WebSocket 连接关闭 | buildId: {} | status: {}", buildId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 传输错误", exception);
        Long buildId = extractBuildId(session);
        if (buildId != null) {
            buildService.unregisterLogSession(buildId);
        }
    }

    private Long extractBuildId(WebSocketSession session) {
        try {
            String path = session.getUri().getPath();
            String[] parts = path.split("/");
            for (int i = 0; i < parts.length; i++) {
                if ("build".equals(parts[i]) && i + 1 < parts.length) {
                    return Long.parseLong(parts[i + 1]);
                }
            }
        } catch (Exception e) {
            log.error("解析 buildId 失败", e);
        }
        return null;
    }
}
