package com.drp.log.websocket;

import com.drp.log.service.AppLogService;
import com.drp.server.entity.Server;
import com.drp.server.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppLogHandler extends TextWebSocketHandler {

    private final AppLogSessionManager sessionManager;
    private final ServerRepository serverRepository;
    private final AppLogService appLogService;

    private final Map<WebSocketSession, ConnectionInfo> sessionInfoMap = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket 连接建立 | sessionId: {}", session.getId());
        sessionManager.broadcast("[INFO] 已连接到日志服务器\n");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("收到消息 | sessionId: {} | payload: {}", session.getId(), payload);

        ConnectionInfo info = sessionInfoMap.get(session);

        if (payload.startsWith("connect:")) {
            String[] parts = payload.substring(8).split(":");
            if (parts.length >= 2) {
                Long serverId = Long.parseLong(parts[0]);
                String logPath = parts[1];

                Server server = serverRepository.selectById(serverId);
                if (server == null) {
                    session.sendMessage(new TextMessage("[ERROR] 服务器不存在\n"));
                    return;
                }

                info = new ConnectionInfo(serverId, logPath);
                sessionInfoMap.put(session, info);
                sessionManager.addSession(serverId, logPath, session);

                session.sendMessage(new TextMessage("[INFO] 正在连接服务器 " + server.getHostname() + " ...\n"));

                appLogService.startTailLog(server, logPath, session);
            } else {
                session.sendMessage(new TextMessage("[ERROR] 无效的连接命令，格式: connect:serverId:logPath\n"));
            }
        } else if (payload.equals("disconnect")) {
            if (info != null) {
                appLogService.stopTailLog(info.serverId, info.logPath);
                sessionManager.removeSession(info.serverId, info.logPath, session);
                sessionInfoMap.remove(session);
                session.sendMessage(new TextMessage("[INFO] 已断开连接\n"));
            }
        } else if (payload.equals("ping")) {
            session.sendMessage(new TextMessage("pong\n"));
        } else {
            session.sendMessage(new TextMessage("[ERROR] 未知命令\n"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        ConnectionInfo info = sessionInfoMap.remove(session);
        if (info != null) {
            appLogService.stopTailLog(info.serverId, info.logPath);
            sessionManager.removeSession(info.serverId, info.logPath, session);
        }
        log.info("WebSocket 连接关闭 | sessionId: {} | status: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 传输错误 | sessionId: {}", session.getId(), exception);
        ConnectionInfo info = sessionInfoMap.remove(session);
        if (info != null) {
            appLogService.stopTailLog(info.serverId, info.logPath);
            sessionManager.removeSession(info.serverId, info.logPath, session);
        }
    }

    public static class ConnectionInfo {
        public final Long serverId;
        public final String logPath;

        public ConnectionInfo(Long serverId, String logPath) {
            this.serverId = serverId;
            this.logPath = logPath;
        }
    }
}
