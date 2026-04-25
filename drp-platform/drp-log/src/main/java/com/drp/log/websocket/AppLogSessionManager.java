package com.drp.log.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class AppLogSessionManager {

    private final Map<Long, Map<String, WebSocketSession>> serverSessions = new ConcurrentHashMap<>();

    public void addSession(Long serverId, String logPath, WebSocketSession session) {
        serverSessions.computeIfAbsent(serverId, k -> new ConcurrentHashMap<>())
                .put(logPath, session);
        log.info("WebSocket 会话添加 | serverId: {} | logPath: {} | sessionId: {}", serverId, logPath, session.getId());
    }

    public void removeSession(Long serverId, String logPath, WebSocketSession session) {
        Map<String, WebSocketSession> sessions = serverSessions.get(serverId);
        if (sessions != null) {
            sessions.remove(logPath, session);
            log.info("WebSocket 会话移除 | serverId: {} | logPath: {} | sessionId: {}", serverId, logPath, session.getId());
        }
    }

    public void sendToServer(Long serverId, String message) {
        Map<String, WebSocketSession> sessions = serverSessions.get(serverId);
        if (sessions != null) {
            sessions.values().forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(message));
                    }
                } catch (IOException e) {
                    log.error("发送消息失败", e);
                }
            });
        }
    }

    public void broadcast(String message) {
        serverSessions.values().forEach(sessions -> {
            sessions.values().forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(message));
                    }
                } catch (IOException e) {
                    log.error("广播消息失败", e);
                }
            });
        });
    }

    public int getConnectionCount() {
        return serverSessions.values().stream()
                .mapToInt(Map::size)
                .sum();
    }

    public void removeAllSessions(Long serverId) {
        Map<String, WebSocketSession> sessions = serverSessions.remove(serverId);
        if (sessions != null) {
            sessions.values().forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.close(CloseStatus.NORMAL);
                    }
                } catch (IOException e) {
                    log.error("关闭会话失败", e);
                }
            });
        }
    }
}
