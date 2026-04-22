package com.drp.server.service;

import com.drp.common.util.AesEncryptUtil;
import com.drp.server.dto.ConnectionTestResult;
import com.drp.server.entity.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SshService {

    public ConnectionTestResult testConnection(Server server) {
        ConnectionTestResult result = new ConnectionTestResult();
        result.setHostname(server.getHostname());
        result.setPort(server.getPort());

        long start = System.currentTimeMillis();
        try {
            String output = execute(server, "echo 'connection_test'");
            if (output.contains("connection_test")) {
                result.setSuccess(true);
                result.setMessage("连接成功");
            } else {
                result.setSuccess(false);
                result.setMessage("执行命令返回异常");
            }
        } catch (Exception e) {
            log.error("SSH连接测试失败: server={}, error={}", server.getHostname(), e.getMessage());
            result.setSuccess(false);
            result.setMessage("连接失败: " + e.getMessage());
        }

        result.setCostMs(System.currentTimeMillis() - start);
        return result;
    }

    public String execute(Server server, String command) throws IOException {
        String password = decryptPassword(server.getPassword());

        try (SshClient client = createClient()) {
            client.start();

            // 连接并等待完成
            ConnectFuture connectFuture = client.connect(
                    server.getUsername(),
                    server.getHostname(),
                    server.getPort()
            );

            ClientSession session = connectFuture.verify(30, TimeUnit.SECONDS).getSession();

            try {
                // 使用密码认证
                session.addPasswordIdentity(password);
                session.auth().verify(30, TimeUnit.SECONDS);

                if (!session.isAuthenticated()) {
                    throw new IOException("SSH authentication failed");
                }

                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                     ByteArrayOutputStream errorStream = new ByteArrayOutputStream()) {

                    ClientChannel channel = session.createExecChannel(command);
                    channel.setOut(outputStream);
                    channel.setErr(errorStream);
                    channel.open().await();

                    Set<ClientChannelEvent> events = EnumSet.of(ClientChannelEvent.CLOSED);
                    channel.waitFor(events, 30000);

                    String output = outputStream.toString(StandardCharsets.UTF_8);
                    String error = errorStream.toString(StandardCharsets.UTF_8);

                    if (!error.isEmpty()) {
                        log.warn("SSH command error output: {}", error);
                    }

                    log.debug("Command executed, output: {}", output);
                    return output;
                }
            } finally {
                session.close();
            }
        }
    }

    private SshClient createClient() {
        SshClient client = SshClient.setUpDefaultClient();
        client.setServerKeyVerifier((clientSession, remoteAddress, serverKey) -> true);
        return client;
    }

    private String decryptPassword(String encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            return "";
        }
        try {
            return AesEncryptUtil.decrypt(encryptedPassword);
        } catch (Exception e) {
            log.error("密码解密失败: {}", e.getMessage());
            return encryptedPassword;
        }
    }
}
