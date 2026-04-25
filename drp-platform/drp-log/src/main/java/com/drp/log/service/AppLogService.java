package com.drp.log.service;

import com.drp.server.entity.Server;
import org.springframework.web.socket.WebSocketSession;

public interface AppLogService {

    void startTailLog(Server server, String logPath, WebSocketSession session);

    void stopTailLog(Long serverId, String logPath);

    void stopAllTailLogs();
}
