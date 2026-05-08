package com.drp.deploy.util;

import com.drp.common.exception.BusinessException;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
@Slf4j
public class SSHUtil {

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 10 * 1000;

    // ==================== Session helpers ====================

    private Session createSession(String hostname, int port, String username,
                                   String password, String privateKey, String passphrase) throws Exception {
        JSch jsch = new JSch();
        if (privateKey != null && !privateKey.isBlank()) {
            byte[] keyBytes = privateKey.getBytes(StandardCharsets.UTF_8);
            jsch.addIdentity("deploy_key", keyBytes, null,
                    passphrase != null ? passphrase.getBytes(StandardCharsets.UTF_8) : null);
        }
        Session session = jsch.getSession(username != null ? username : "root", hostname, port);
        session.setTimeout(30_000);
        session.setServerAliveInterval(30_000);
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
        session.connect(DEFAULT_CONNECT_TIMEOUT_MS);
        return session;
    }

    @FunctionalInterface
    private interface SftpOperation<T> {
        T execute(com.jcraft.jsch.ChannelSftp channel) throws Exception;
    }

    private <T> T withSftp(String hostname, Integer port, String username, String password,
                            String privateKey, String passphrase, SftpOperation<T> operation) {
        Session session = null;
        com.jcraft.jsch.ChannelSftp channel = null;
        try {
            int sshPort = port != null && port > 0 ? port : 22;
            session = createSession(hostname, sshPort, username, password, privateKey, passphrase);
            channel = (com.jcraft.jsch.ChannelSftp) session.openChannel("sftp");
            channel.connect(DEFAULT_CONNECT_TIMEOUT_MS);
            return operation.execute(channel);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("SFTP操作失败 | server: {}", hostname, e);
            throw new BusinessException("SFTP操作失败: " + e.getMessage());
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    // ==================== SFTP file operations (cross-platform, protocol-based) ====================

    /**
     * 递归创建远程目录（替代 Linux mkdir -p 命令）
     */
    public void createDirectories(String hostname, Integer port, String username, String password,
                                   String privateKey, String passphrase, String remotePath) {
        String normalized = remotePath.replace('\\', '/');
        String[] parts = java.util.Arrays.stream(normalized.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        withSftp(hostname, port, username, password, privateKey, passphrase, channel -> {
            StringBuilder currentPath = new StringBuilder();
            for (String part : parts) {
                currentPath.append('/').append(part);
                try {
                    channel.mkdir(currentPath.toString());
                } catch (Exception e) {
                    try {
                        SftpATTRS attrs = channel.stat(currentPath.toString());
                        if (!attrs.isDir()) {
                            throw new BusinessException("路径已存在但不是目录: " + currentPath);
                        }
                    } catch (Exception ignored) {
                        throw new BusinessException("无法创建目录: " + currentPath + " | " + e.getMessage());
                    }
                }
            }
            log.info("SFTP创建目录 | {} | 路径: {}", hostname, normalized);
            return null;
        });
    }

    /**
     * 检查远程路径是否存在（替代 Linux [ -f / -d ] 判断）
     */
    public boolean pathExists(String hostname, Integer port, String username, String password,
                               String privateKey, String passphrase, String remotePath) {
        return withSftp(hostname, port, username, password, privateKey, passphrase, channel -> {
            try {
                channel.stat(remotePath);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * 创建或更新符号链接（替代 Linux ln -snf 命令）
     */
    public void createOrUpdateSymlink(String hostname, Integer port, String username, String password,
                                       String privateKey, String passphrase, String target, String linkPath) {
        final String symlinkTarget = resolveSymlinkTarget(target, linkPath);

        try {
            withSftp(hostname, port, username, password, privateKey, passphrase, channel -> {
                try {
                    channel.symlink(symlinkTarget, linkPath);
                } catch (Exception e) {
                    try {
                        SftpATTRS attrs = channel.lstat(linkPath);
                        if (attrs.isLink() || attrs.isReg()) {
                            channel.rm(linkPath);
                        } else if (attrs.isDir()) {
                            channel.rmdir(linkPath);
                        }
                    } catch (Exception ignored) {
                    }
                    channel.symlink(symlinkTarget, linkPath);
                }
                log.info("SFTP创建符号链接 | {} | {} → {}", hostname, linkPath, symlinkTarget);
                return null;
            });
        } catch (Exception sftpEx) {
            log.warn("SFTP符号链接失败，回退到exec通道 | {} | {}", hostname, sftpEx.getMessage());
            String cmd = "ln -snf " + target + " " + linkPath;
            executeCommand(hostname, port, username, password, privateKey, passphrase, cmd, 10);
        }
    }

    /**
     * Compute a relative symlink target when target and link reside in the same directory tree.
     * SFTP servers often handle relative targets more reliably than absolute ones.
     */
    private String resolveSymlinkTarget(String target, String linkPath) {
        try {
            String[] targetParts = target.replace('\\', '/').split("/");
            String[] linkParts = linkPath.replace('\\', '/').split("/");
            int commonLen = 0;
            int maxCommon = Math.min(targetParts.length - 1, linkParts.length - 1);
            for (int i = 0; i < maxCommon; i++) {
                if (targetParts[i].equals(linkParts[i])) commonLen++;
                else break;
            }
            if (commonLen > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = commonLen; i < linkParts.length - 1; i++) {
                    sb.append("../");
                }
                for (int i = commonLen; i < targetParts.length; i++) {
                    if (sb.length() > 0) sb.append("/");
                    sb.append(targetParts[i]);
                }
                if (!sb.toString().startsWith("../")) {
                    return sb.toString();
                }
            }
        } catch (Exception ignored) {
        }
        return target;
    }

    /**
     * 读取符号链接目标路径（替代 Linux readlink 命令）
     */
    public String readSymlink(String hostname, Integer port, String username, String password,
                               String privateKey, String passphrase, String linkPath) {
        return withSftp(hostname, port, username, password, privateKey, passphrase, channel -> {
            try {
                SftpATTRS attrs = channel.lstat(linkPath);
                if (attrs.isLink()) {
                    return channel.readlink(linkPath);
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        });
    }

    /**
     * 删除远程文件或目录（替代 Linux rm 命令）
     */
    public void deleteRemote(String hostname, Integer port, String username, String password,
                              String privateKey, String passphrase, String remotePath) {
        withSftp(hostname, port, username, password, privateKey, passphrase, channel -> {
            try {
                SftpATTRS attrs = channel.stat(remotePath);
                if (attrs.isDir()) {
                    channel.rmdir(remotePath);
                } else {
                    channel.rm(remotePath);
                }
                log.info("SFTP删除 | {} | path: {}", hostname, remotePath);
            } catch (Exception e) {
                log.warn("SFTP删除失败（已忽略）| {} | path: {} | {}", hostname, remotePath, e.getMessage());
            }
            return null;
        });
    }

    /**
     * 列出远程目录内容
     */
    @SuppressWarnings("unchecked")
    public List<com.jcraft.jsch.ChannelSftp.LsEntry> listDirectory(String hostname, Integer port,
                                                                    String username, String password,
                                                                    String privateKey, String passphrase,
                                                                    String remotePath) {
        return withSftp(hostname, port, username, password, privateKey, passphrase, channel -> {
            List<com.jcraft.jsch.ChannelSftp.LsEntry> entries = new ArrayList<>();
            try {
                java.util.Vector<com.jcraft.jsch.ChannelSftp.LsEntry> vector = channel.ls(remotePath);
                for (com.jcraft.jsch.ChannelSftp.LsEntry entry : vector) {
                    String name = entry.getFilename();
                    if (".".equals(name) || "..".equals(name)) continue;
                    entries.add(entry);
                }
            } catch (Exception e) {
                log.warn("SFTP列出目录失败 | {} | path: {} | {}", hostname, remotePath, e.getMessage());
            }
            return entries;
        });
    }

    // ==================== File transfer ====================

    /**
     * 通过 SFTP 上传本地文件到远程服务器
     */
    public void uploadFile(String hostname, Integer port, String username, String password,
                           String privateKey, String passphrase,
                           String localFilePath, String remoteDir, String remoteFileName) {
        withSftp(hostname, port, username, password, privateKey, passphrase, channel -> {
            String normalizedDir = remoteDir.replace('\\', '/');
            String normalizedName = remoteFileName.replace('\\', '/');
            String remoteTargetPath = normalizedDir + "/" + normalizedName;
            int lastSlash = remoteTargetPath.lastIndexOf('/');
            if (lastSlash > 0) {
                String parentDir = remoteTargetPath.substring(0, lastSlash);
                ensureRemoteDirectories(channel, parentDir);
            } else {
                ensureRemoteDirectories(channel, normalizedDir);
            }
            Path localPath = Path.of(localFilePath);
            long fileSize = Files.size(localPath);
            channel.put(localFilePath, remoteTargetPath);
            log.info("SFTP上传完成 | server: {} | file: {} | size: {} KB",
                    hostname, remoteFileName, fileSize / 1024);
            return null;
        });
    }

    private void ensureRemoteDirectories(com.jcraft.jsch.ChannelSftp channel, String remotePath) throws Exception {
        String normalized = remotePath.replace('\\', '/');
        String[] parts = java.util.Arrays.stream(normalized.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        StringBuilder currentPath = new StringBuilder();
        for (String part : parts) {
            currentPath.append('/').append(part);
            try {
                channel.mkdir(currentPath.toString());
            } catch (Exception e) {
                SftpATTRS attrs = channel.stat(currentPath.toString());
                if (!attrs.isDir()) {
                    throw new BusinessException("远程路径不是目录: " + currentPath);
                }
            }
        }
    }

    /**
     * HTTP 下载到本地临时文件，再通过 SFTP 上传到远程服务器（替代远程 curl 下载方式）
     */
    public String downloadAndUploadArtifact(String hostname, Integer port, String username,
                                             String password, String privateKey, String passphrase,
                                             String artifactUrl, String remoteDir, String remoteFileName) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("deploy-artifact-", ".tmp");
            log.info("HTTP下载制品 | URL: {}", artifactUrl);

            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(artifactUrl))
                    .timeout(Duration.ofMinutes(10))
                    .GET()
                    .build();

            HttpResponse<Path> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofFile(tempFile));

            int status = response.statusCode();
            if (status < 200 || status >= 400) {
                throw new BusinessException("下载制品失败: HTTP " + status);
            }

            long fileSize = Files.size(tempFile);
            log.info("HTTP下载完成 | 状态码: {} | 大小: {} KB", status, fileSize / 1024);

            uploadFile(hostname, port, username, password, privateKey, passphrase,
                    tempFile.toString(), remoteDir, remoteFileName);

            return "UPLOAD_OK:" + remoteFileName;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("制品传输失败 | server: {} | URL: {}", hostname, artifactUrl, e);
            throw new BusinessException("制品传输失败: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (Exception ignored) {}
            }
        }
    }

    // ==================== Command execution (for service management) ====================

    public String executeCommand(String hostname, Integer port, String username, String password,
                                  String privateKey, String passphrase, String command) {
        return executeCommand(hostname, port, username, password, privateKey, passphrase, command, DEFAULT_TIMEOUT_SECONDS);
    }

    public String executeCommand(String hostname, Integer port, String username, String password,
                                  String privateKey, String passphrase, String command, int timeoutSeconds) {
        Session session = null;
        com.jcraft.jsch.ChannelExec channel = null;
        try {
            int sshPort = port != null && port > 0 ? port : 22;
            session = createSession(hostname, sshPort, username, password, privateKey, passphrase);

            log.info("\n╔══ SSH EXEC ══════════════════════════════════════════════════════\n║  server : {}:{}\n║  cmd    : {}\n╚══════════════════════════════════════════════════════════════════",
                    hostname, sshPort, command);

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

    public String executeCommandSilently(String hostname, Integer port, String username, String password,
                                          String privateKey, String passphrase, String command, int timeoutSeconds) {
        Session session = null;
        com.jcraft.jsch.ChannelExec channel = null;
        try {
            int sshPort = port != null && port > 0 ? port : 22;
            session = createSession(hostname, sshPort, username, password, privateKey, passphrase);

            log.info("\n╔══ SSH EXEC (silent) ═════════════════════════════════════════════\n║  server : {}:{}\n║  cmd    : {}\n╚══════════════════════════════════════════════════════════════════",
                    hostname, sshPort, command);

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

    public boolean checkConnectivity(String hostname, Integer port, String username, String password,
                                      String privateKey, String passphrase) {
        try {
            withSftp(hostname, port, username, password, privateKey, passphrase, channel -> {
                channel.stat("/");
                return null;
            });
            return true;
        } catch (Exception e) {
            log.warn("服务器连通性检查失败 | server: {}", hostname, e);
            return false;
        }
    }
}
