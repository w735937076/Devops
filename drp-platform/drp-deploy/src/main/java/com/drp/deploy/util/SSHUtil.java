package com.drp.deploy.util;

import com.drp.common.exception.BusinessException;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Component
@Slf4j
public class SSHUtil {

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 10 * 1000;

    public String executeCommand(String hostname, Integer port, String username, String password, String privateKey, String passphrase, String command) {
        return executeCommand(hostname, port, username, password, privateKey, passphrase, command, DEFAULT_TIMEOUT_SECONDS);
    }

    public String executeCommand(String hostname, Integer port, String username, String password, String privateKey, String passphrase, String command, int timeoutSeconds) {
        Session session = null;
        com.jcraft.jsch.ChannelExec channel = null;
        try {
            int sshPort = port != null ? port : 22;
            JSch jsch = new JSch();

            if (privateKey != null && !privateKey.isBlank()) {
                byte[] keyBytes = privateKey.getBytes(StandardCharsets.UTF_8);
                jsch.addIdentity("deploy_key", keyBytes, null, passphrase != null ? passphrase.getBytes(StandardCharsets.UTF_8) : null);
            }

            session = jsch.getSession(username != null ? username : "root", hostname, sshPort);
            session.setTimeout(timeoutSeconds * 1000);
            session.setServerAliveInterval(30000);

            if (password != null && !password.isBlank()) {
                session.setPassword(password);
            }

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications",
                    privateKey != null && !privateKey.isBlank()
                            ? "publickey"
                            : "password,keyboard-interactive");
            config.put("ConnectTimeout", String.valueOf(DEFAULT_CONNECT_TIMEOUT_MS));
            session.setConfig(config);

            log.info("\n╔══ SSH EXEC ══════════════════════════════════════════════════════\n║  server : {}:{}\n║  cmd    : {}\n╚══════════════════════════════════════════════════════════════════",
                    hostname, sshPort, command);
            session.connect(DEFAULT_CONNECT_TIMEOUT_MS);

            channel = (com.jcraft.jsch.ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);

            java.io.ByteArrayOutputStream errStream = new java.io.ByteArrayOutputStream();
            channel.setErrStream(errStream);

            InputStream in = channel.getInputStream();
            channel.connect(DEFAULT_CONNECT_TIMEOUT_MS);

            StringBuilder output = new StringBuilder();
            byte[] buf = new byte[1024];
            long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);

            while (true) {
                while (in.available() > 0) {
                    int len = in.read(buf);
                    if (len > 0) {
                        output.append(new String(buf, 0, len, StandardCharsets.UTF_8));
                    }
                }
                if (channel.isClosed()) {
                    // drain any remaining bytes after close signal
                    while (in.available() > 0) {
                        int len = in.read(buf);
                        if (len > 0) {
                            output.append(new String(buf, 0, len, StandardCharsets.UTF_8));
                        }
                    }
                    break;
                }
                if (System.currentTimeMillis() > deadline) {
                    throw new BusinessException("远程命令执行超时 | timeout: " + timeoutSeconds + "s");
                }
                Thread.sleep(100);
            }

            int exitStatus = channel.getExitStatus();
            String stderr = errStream.toString(StandardCharsets.UTF_8);
            String outTrimmed = output.toString().trim();
            if (exitStatus == 0) {
                log.info("\n╔══ SSH RESULT ════════════════════════════════════════════════════\n║  server : {}:{}\n║  exit   : {}\n║  output : {}\n╚══════════════════════════════════════════════════════════════════",
                        hostname, sshPort, exitStatus, outTrimmed.isEmpty() ? "(empty)" : outTrimmed);
            } else {
                log.error("\n╔══ SSH FAILED ════════════════════════════════════════════════════\n║  server : {}:{}\n║  exit   : {}\n║  output : {}\n║  stderr : {}\n╚══════════════════════════════════════════════════════════════════",
                        hostname, sshPort, exitStatus, outTrimmed.isEmpty() ? "(empty)" : outTrimmed,
                        stderr.isBlank() ? "(empty)" : stderr.trim());
            }

            if (exitStatus != 0) {
                String detail = output.toString();
                if (!stderr.isBlank()) {
                    detail = detail + (detail.isBlank() ? "" : "\n") + "[stderr] " + stderr;
                }
                throw new BusinessException("远程命令执行失败 | exitCode: " + exitStatus + " | output: " + detail);
            }

            return output.toString();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("远程命令执行异常 | server: {}", hostname, e);
            throw new BusinessException("远程命令执行失败: " + e.getMessage());
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    public String executeCommandSilently(String hostname, Integer port, String username, String password, String privateKey, String passphrase, String command, int timeoutSeconds) {
        Session session = null;
        com.jcraft.jsch.ChannelExec channel = null;
        try {
            int sshPort = port != null ? port : 22;
            JSch jsch = new JSch();

            if (privateKey != null && !privateKey.isBlank()) {
                byte[] keyBytes = privateKey.getBytes(StandardCharsets.UTF_8);
                jsch.addIdentity("deploy_key", keyBytes, null, passphrase != null ? passphrase.getBytes(StandardCharsets.UTF_8) : null);
            }

            session = jsch.getSession(username != null ? username : "root", hostname, sshPort);
            session.setTimeout(timeoutSeconds * 1000);

            if (password != null && !password.isBlank()) {
                session.setPassword(password);
            }

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications",
                    privateKey != null && !privateKey.isBlank()
                            ? "publickey"
                            : "password,keyboard-interactive");
            config.put("ConnectTimeout", String.valueOf(DEFAULT_CONNECT_TIMEOUT_MS));
            session.setConfig(config);

            log.info("\n╔══ SSH EXEC (silent) ═════════════════════════════════════════════\n║  server : {}:{}\n║  cmd    : {}\n╚══════════════════════════════════════════════════════════════════",
                    hostname, sshPort, command);
            session.connect(DEFAULT_CONNECT_TIMEOUT_MS);

            channel = (com.jcraft.jsch.ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);

            InputStream in = channel.getInputStream();
            channel.connect(DEFAULT_CONNECT_TIMEOUT_MS);

            StringBuilder output = new StringBuilder();
            byte[] buf = new byte[1024];
            long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);

            while (true) {
                while (in.available() > 0) {
                    int len = in.read(buf);
                    if (len > 0) {
                        output.append(new String(buf, 0, len, StandardCharsets.UTF_8));
                    }
                }
                if (channel.isClosed()) {
                    // Blocking drain: in.available() may return 0 even when data is buffered
                    int len;
                    while (in.available() > 0 && (len = in.read(buf)) > 0) {
                        output.append(new String(buf, 0, len, StandardCharsets.UTF_8));
                    }
                    break;
                }
                if (System.currentTimeMillis() > deadline) {
                    break;
                }
                Thread.sleep(100);
            }

            String outTrimmed = output.toString().trim();
            log.info("\n╔══ SSH RESULT (silent) ═══════════════════════════════════════════\n║  server : {}:{}\n║  output : {}\n╚══════════════════════════════════════════════════════════════════",
                    hostname, sshPort, outTrimmed.isEmpty() ? "(empty)" : outTrimmed);
            return output.toString();
        } catch (Exception e) {
            log.error("远程命令执行异常（静默）| server: {}", hostname, e);
            return "ERROR: " + e.getMessage();
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    public boolean checkConnectivity(String hostname, Integer port, String username, String password, String privateKey, String passphrase) {
        try {
            String result = executeCommandSilently(hostname, port, username, password, privateKey, passphrase, "echo ok", 10);
            return "ok".equals(result.trim());
        } catch (Exception e) {
            log.warn("服务器连通性检查失败 | server: {}", hostname, e);
            return false;
        }
    }

    public String downloadArtifact(String hostname, Integer port, String username, String password, String privateKey, String passphrase, String artifactUrl, String deployPath, String targetFileName) {
        String downloadCmd = String.format(
                "curl -fsSL -w \"\\n[HTTP:%%{http_code}] %%{size_download}bytes\" \"%s\" -o \"%s/%s\" 2>&1",
                artifactUrl, deployPath, targetFileName);
        return executeCommand(hostname, port, username, password, privateKey, passphrase, downloadCmd, 300);
    }

    /**
     * Upload a local file to the remote server via SFTP.
     * This avoids the need for the remote server to reach the build server over HTTP.
     */
    public void uploadFile(String hostname, Integer port, String username, String password,
                           String privateKey, String passphrase,
                           String localFilePath, String remoteDir, String remoteFileName) {
        Session session = null;
        com.jcraft.jsch.ChannelSftp channel = null;
        try {
            int sshPort = port != null ? port : 22;
            JSch jsch = new JSch();

            if (privateKey != null && !privateKey.isBlank()) {
                byte[] keyBytes = privateKey.getBytes(StandardCharsets.UTF_8);
                jsch.addIdentity("deploy_key", keyBytes, null,
                        passphrase != null ? passphrase.getBytes(StandardCharsets.UTF_8) : null);
            }

            session = jsch.getSession(username != null ? username : "root", hostname, sshPort);
            session.setTimeout(300_000);

            if (password != null && !password.isBlank()) {
                session.setPassword(password);
            }

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications",
                    privateKey != null && !privateKey.isBlank()
                            ? "publickey"
                            : "password,keyboard-interactive");
            session.setConfig(config);

            log.info("SFTP上传 | server: {}:{} | local: {} → remote: {}/{}", hostname, sshPort, localFilePath, remoteDir, remoteFileName);
            session.connect(DEFAULT_CONNECT_TIMEOUT_MS);

            channel = (com.jcraft.jsch.ChannelSftp) session.openChannel("sftp");
            channel.connect(DEFAULT_CONNECT_TIMEOUT_MS);

            // Ensure remote directory exists
            try {
                channel.mkdir(remoteDir);
            } catch (com.jcraft.jsch.SftpException ignored) {
                // Directory may already exist
            }
            channel.cd(remoteDir);
            channel.put(localFilePath, remoteFileName);

            log.info("SFTP上传完成 | server: {} | file: {}", hostname, remoteFileName);
        } catch (Exception e) {
            log.error("SFTP上传失败 | server: {}", hostname, e);
            throw new BusinessException("制品上传失败: " + e.getMessage());
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
