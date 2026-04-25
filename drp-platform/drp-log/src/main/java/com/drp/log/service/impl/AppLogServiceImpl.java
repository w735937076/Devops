package com.drp.log.service.impl;

import com.drp.log.service.AppLogService;
import com.drp.server.entity.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.core.CoreModuleProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class AppLogServiceImpl implements AppLogService {

    private final Map<String, Process> runningProcesses = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final AtomicInteger threadIdCounter = new AtomicInteger(1);

    @Override
    public void startTailLog(Server server, String logPath, WebSocketSession session) {
        String processKey = server.getId() + ":" + logPath;

        if (runningProcesses.containsKey(processKey)) {
            try {
                session.sendMessage(new TextMessage("[WARN] 该日志已在被其他用户查看\n"));
            } catch (Exception e) {
                log.error("发送消息失败", e);
            }
            return;
        }

        executor.submit(() -> {
            String threadName = "tail-log-" + threadIdCounter.getAndIncrement();
            Thread.currentThread().setName(threadName);

            try {
                String command = "tail -f " + logPath;
                log.info("执行远程日志命令 | server: {} | command: {}", server.getHostname(), command);

                ProcessBuilder pb = new ProcessBuilder();
                pb.command("ssh",
                        "-o", "StrictHostKeyChecking=no",
                        "-o", "UserKnownHostsFile=/dev/null",
                        "-p", String.valueOf(server.getPort() != null ? server.getPort() : 22),
                        (server.getUsername() != null ? server.getUsername() : "root") + "@" + server.getHostname(),
                        command);

                pb.redirectErrorStream(true);
                Process process = pb.start();
                runningProcesses.put(processKey, process);

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!session.isOpen()) {
                            break;
                        }
                        try {
                            session.sendMessage(new TextMessage(line + "\n"));
                        } catch (Exception e) {
                            log.error("发送日志行失败", e);
                            break;
                        }
                    }
                }

                int exitCode = process.waitFor();
                log.info("日志进程结束 | server: {} | logPath: {} | exitCode: {}", server.getHostname(), logPath, exitCode);
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage("[INFO] 日志流已结束，进程退出码: " + exitCode + "\n"));
                }

            } catch (Exception e) {
                log.error("执行远程日志失败 | server: {} | logPath: {}", server.getHostname(), logPath, e);
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage("[ERROR] " + e.getMessage() + "\n"));
                    }
                } catch (Exception sendError) {
                    log.error("发送错误消息失败", sendError);
                }
            } finally {
                runningProcesses.remove(processKey);
                if (processKey.endsWith(":" + logPath)) {
                    Process p = runningProcesses.remove(processKey);
                    if (p != null && p.isAlive()) {
                        p.destroy();
                    }
                }
            }
        });
    }

    @Override
    public void stopTailLog(Long serverId, String logPath) {
        String processKey = serverId + ":" + logPath;
        Process process = runningProcesses.remove(processKey);
        if (process != null && process.isAlive()) {
            process.destroy();
            log.info("停止日志流 | serverId: {} | logPath: {}", serverId, logPath);
        }
    }

    @Override
    public void stopAllTailLogs() {
        runningProcesses.forEach((key, process) -> {
            if (process.isAlive()) {
                process.destroy();
                log.info("停止日志流 | key: {}", key);
            }
        });
        runningProcesses.clear();
    }
}
