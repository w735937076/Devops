package com.drp.deploy.executor;

import com.drp.build.entity.BuildRecord;
import com.drp.build.repository.BuildRecordRepository;
import com.drp.common.exception.BusinessException;
import com.drp.deploy.dto.DeployCheckItemDTO;
import com.drp.deploy.dto.DeployServerDTO;
import com.drp.deploy.entity.DeployRecord;
import com.drp.deploy.enums.DeployStatus;
import com.drp.deploy.repository.DeployRecordRepository;
import com.drp.deploy.util.SSHUtil;
import com.drp.project.entity.Project;
import com.drp.project.repository.ProjectRepository;
import com.drp.project.service.CredentialService;
import com.drp.server.entity.ProjectServer;
import com.drp.server.entity.Server;
import com.drp.server.repository.ProjectServerRepository;
import com.drp.server.repository.ServerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeployExecutor {

    @Value("${drp.deploy.artifact-base-url:}")
    private String artifactBaseUrl;

    @Value("${server.port:8081}")
    private int serverPort;

    private final SSHUtil sshUtil;
    private final ProjectRepository projectRepository;
    private final BuildRecordRepository buildRecordRepository;
    private final ServerRepository serverRepository;
    private final ProjectServerRepository projectServerRepository;
    private final DeployRecordRepository deployRecordRepository;
    private final CredentialService credentialService;
    private final ObjectMapper objectMapper;

    public List<DeployCheckItemDTO> executePreCheck(DeployRecord record) {
        List<DeployCheckItemDTO> checks = new ArrayList<>();
        Project project = projectRepository.selectById(record.getProjectId());
        BuildRecord build = buildRecordRepository.selectById(record.getBuildId());

        checks.add(check("项目状态", project != null ? "PASS" : "FAIL",
                project != null ? "项目存在" : "项目不存在"));
        checks.add(check("构建版本", build != null && build.getStatus() == 2 ? "PASS" : "FAIL",
                build != null && build.getStatus() == 2 ? "构建成功" : "构建记录不存在或未成功"));

        List<ProjectServer> bindings = projectServerRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProjectServer>()
                        .eq(ProjectServer::getProjectId, record.getProjectId())
                        .eq(ProjectServer::getEnv, record.getEnv())
                        .eq(ProjectServer::getDeleted, false));

        checks.add(check("服务器映射", !bindings.isEmpty() ? "PASS" : "FAIL",
                !bindings.isEmpty() ? "已绑定 " + bindings.size() + " 台服务器" : "未绑定任何服务器"));

        for (ProjectServer binding : bindings) {
            Server server = serverRepository.selectById(binding.getServerId());
            if (server != null && server.getDeleted() != null && !server.getDeleted()) {
                boolean reachable = sshUtil.checkConnectivity(
                        server.getHostname(),
                        server.getPort(),
                        server.getUsername(),
                        server.getDecryptedPassword(),
                        server.getDecryptedPrivateKey(),
                        server.getPrivateKeyPassphrase());
                checks.add(check("服务器连通性 [" + server.getHostname() + "]", reachable ? "PASS" : "FAIL",
                        reachable ? "可达" : "不可达，请检查网络和认证配置"));
            }
        }

        return checks;
    }

    public void executeDeploy(DeployRecord record, List<DeployServerDTO> servers, String strategy, boolean rollbackMode) {
        record.setStatus(DeployStatus.RUNNING.getCode());
        record.setCurrentStep("预检");
        record.setSummary("部署执行中");
        deployRecordRepository.updateById(record);

        Project project = projectRepository.selectById(record.getProjectId());
        BuildRecord build = buildRecordRepository.selectById(record.getBuildId());

        if (project == null || build == null) {
            throw new BusinessException("项目或构建记录不存在");
        }

        List<Map<String, Object>> artifacts = parseArtifacts(build.getArtifacts());
        if (artifacts == null || artifacts.isEmpty()) {
            throw new BusinessException("构建产物为空，请先完成构建");
        }

        for (DeployServerDTO serverDto : servers) {
            Server server = serverRepository.selectById(serverDto.getId());
            if (server == null) {
                throw new BusinessException("服务器不存在: " + serverDto.getId());
            }

            ProjectServer binding = projectServerRepository.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProjectServer>()
                            .eq(ProjectServer::getProjectId, record.getProjectId())
                            .eq(ProjectServer::getServerId, server.getId())
                            .eq(ProjectServer::getEnv, record.getEnv())
                            .eq(ProjectServer::getDeleted, false));

            if (binding == null) {
                throw new BusinessException("服务器未绑定到项目环境: " + server.getHostname());
            }

            String deployPath = binding.getDeployPath();
            executeOnServer(record, server, deployPath, artifacts, strategy, rollbackMode, project, build);
        }

        record.setCurrentStep("健康检查");
        record.setLogContent(record.getLogContent() + "\n[INFO] 开始健康检查...\n");
        deployRecordRepository.updateById(record);

        List<DeployCheckItemDTO> healthChecks = performHealthCheck(record, servers);

        boolean allHealthy = healthChecks.stream()
                .allMatch(c -> "PASS".equals(c.getStatus()) || "WARNING".equals(c.getStatus()));

        record.setStatus(allHealthy ? DeployStatus.SUCCESS.getCode() : DeployStatus.FAILED.getCode());
        record.setCurrentStep(allHealthy ? "已完成" : "健康检查失败");
        record.setSummary(allHealthy ? "部署成功" : "健康检查未通过");
        record.setHealthChecks(toJson(healthChecks));
        record.setEndTime(java.time.LocalDateTime.now());
        deployRecordRepository.updateById(record);
    }

    private void executeOnServer(DeployRecord record, Server server, String deployPath,
                                 List<Map<String, Object>> artifacts, String strategy,
                                 boolean rollbackMode, Project project, BuildRecord build) {
        String stepName = rollbackMode ? "回滚" : "部署";
        String host = server.getHostname();
        String user = server.getUsername();
        String pwd = server.getDecryptedPassword();
        String key = server.getDecryptedPrivateKey();
        String pp = server.getPrivateKeyPassphrase();
        int port = server.getPort() != null && server.getPort() > 0 ? server.getPort() : 22;

        String log = record.getLogContent();
        log = appendStep(log, host, "====== " + stepName + " 开始 ======");
        record.setLogContent(log);
        deployRecordRepository.updateById(record);

        log = appendStep(log, host, "步骤 1/5 » 准备目标目录");
        record.setLogContent(log);
        deployRecordRepository.updateById(record);
        sshUtil.createDirectories(host, port, user, pwd, key, pp, deployPath);
        log = appendLog(log, "  目标目录就绪: " + deployPath);

        log = appendStep(log, host, "步骤 2/5 » 记录当前版本");
        record.setLogContent(log);
        deployRecordRepository.updateById(record);
        if (!rollbackMode) {
            String previousVersion = sshUtil.readSymlink(host, port, user, pwd, key, pp, deployPath + "/current");
            log = appendLog(log, "  当前版本: " + (previousVersion != null ? previousVersion : "(无)"));
        } else {
            log = appendLog(log, "  回滚模式，跳过版本记录");
        }
        record.setLogContent(log);
        deployRecordRepository.updateById(record);

        log = appendStep(log, host, "步骤 3/5 » 上传制品");
        record.setLogContent(log);
        deployRecordRepository.updateById(record);

        String versionDir;
        if ("BLUE_GREEN".equals(strategy)) {
            String currentColor = detectCurrentColor(server, deployPath);
            String newColor = "blue".equals(currentColor) ? "green" : "blue";
            versionDir = deployPath + "/" + newColor;
        } else if ("GRAY".equals(strategy)) {
            versionDir = deployPath + "/gray";
        } else {
            String ts = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            versionDir = deployPath + "/v_" + ts;
        }

        sshUtil.createDirectories(host, port, user, pwd, key, pp, versionDir);
        log = appendLog(log, "  版本目录: " + versionDir);
        record.setLogContent(log);
        deployRecordRepository.updateById(record);

        for (Map<String, Object> artifact : artifacts) {
            String artifactName = (String) artifact.get("name");
            Object pathObj = artifact.get("path");
            Object downloadUrlObj = artifact.get("downloadUrl");
            if (pathObj == null && downloadUrlObj == null) continue;

            String targetName = resolveTargetName(artifactName, pathObj, downloadUrlObj);

            if (pathObj != null) {
                java.io.File localFile = new java.io.File(pathObj.toString());
                if (!localFile.exists() || !localFile.isFile()) {
                    throw new BusinessException("制品文件不存在: " + pathObj);
                }
                log = appendLog(log, "  SFTP上传: " + localFile.getName()
                        + " (" + localFile.length() / 1024 + " KB) → " + versionDir);
                record.setLogContent(log);
                deployRecordRepository.updateById(record);
                sshUtil.uploadFile(host, port, user, pwd, key, pp,
                        localFile.getAbsolutePath(), versionDir, targetName);
                log = appendLog(log, "  上传完成 ✓ " + targetName);
            } else {
                String downloadUrl = downloadUrlObj.toString();
                if (!downloadUrl.startsWith("http://") && !downloadUrl.startsWith("https://")
                        && !downloadUrl.startsWith("ftp://")) {
                    downloadUrl = guessArtifactBaseUrl() + downloadUrl;
                }
                log = appendLog(log, "  HTTP下载+SFTP上传: " + downloadUrl);
                record.setLogContent(log);
                deployRecordRepository.updateById(record);
                String dlResult = sshUtil.downloadAndUploadArtifact(
                        host, port, user, pwd, key, pp,
                        downloadUrl, versionDir, targetName);
                log = appendLog(log, "  上传完成 ✓ " + dlResult);
            }
            record.setLogContent(log);
            deployRecordRepository.updateById(record);
        }

        log = appendStep(log, host, "步骤 4/5 » 切换版本 (" + displayStrategy(strategy) + ")");
        record.setLogContent(log);
        deployRecordRepository.updateById(record);
        sshUtil.createOrUpdateSymlink(host, port, user, pwd, key, pp,
                versionDir, deployPath + "/current");
        log = appendLog(log, "  符号链接已更新: current → " + versionDir);
        record.setLogContent(log);
        deployRecordRepository.updateById(record);

        log = appendStep(log, host, "步骤 5/5 » 重启服务");
        record.setLogContent(log);
        deployRecordRepository.updateById(record);
        String restartResult = restartService(server, project, deployPath);
        log = appendLog(log, "  服务重启结果: " + restartResult.trim());

        log = appendStep(log, host, "====== " + stepName + " 完成 ======");
        record.setLogContent(log);
        deployRecordRepository.updateById(record);
    }

    private String resolveTargetName(String artifactName, Object pathObj, Object downloadUrlObj) {
        String normalizedName = artifactName != null ? artifactName.replace("\\", "/") : "";
        if (normalizedName.contains("/")) {
            return trimLeadingSlash(normalizedName);
        }

        String fullPath = null;
        if (pathObj != null) {
            fullPath = pathObj.toString().replace("\\", "/");
        } else if (downloadUrlObj != null) {
            fullPath = extractPathFromUrl(downloadUrlObj.toString());
        }

        if (fullPath != null) {
            String relative = extractRelativeFromBuildPath(fullPath);
            if (relative != null) {
                return trimLeadingSlash(relative);
            }
            String fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
            if (!fileName.isBlank()) {
                return fileName;
            }
        }

        if (!normalizedName.isBlank()) {
            return trimLeadingSlash(normalizedName);
        }
        return "app";
    }

    private String extractPathFromUrl(String url) {
        try {
            String path = URI.create(url).getPath();
            if (path != null && !path.isBlank()) {
                return path;
            }
        } catch (Exception ignored) {
        }
        return url.contains("/") ? url : null;
    }

    private String extractRelativeFromBuildPath(String fullPath) {
        for (String marker : new String[]{"/dist/", "/build/", "/output/", "/target/classes/"}) {
            int idx = fullPath.indexOf(marker);
            if (idx >= 0) {
                String relative = fullPath.substring(idx + marker.length());
                if (!relative.isBlank()) {
                    return relative;
                }
            }
        }
        return null;
    }

    private String trimLeadingSlash(String path) {
        String result = path;
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }

    /**
     * @deprecated 策略切换已通过 SFTP 协议操作实现（createOrUpdateSymlink），不再使用 shell 命令。
     *             保留此方法供回滚场景扩展使用。
     */
    @Deprecated
    private String executeStrategySwitch(Server server, String deployPath, String strategy, boolean rollbackMode, Project project) {
        throw new UnsupportedOperationException("策略切换已迁移至 SFTP 协议操作，请使用 createOrUpdateSymlink");
    }

    private String restartService(Server server, Project project, String deployPath) {
        String serviceName = project.getName().replace("-", "").toLowerCase();
        // Source login profiles so JAVA_HOME / PATH are available (SSH exec runs a non-login shell)
        String sourceEnv = ". /etc/profile 2>/dev/null; "
                + "[ -f ~/.bash_profile ] && . ~/.bash_profile 2>/dev/null; "
                + "[ -f ~/.bashrc ] && . ~/.bashrc 2>/dev/null; ";
        // Use 'restart' (not 'stop; start') so that stop-failure never blocks start
        String restartCmd = sourceEnv
                + "if systemctl cat " + serviceName + " >/dev/null 2>&1; then "
                + "systemctl restart " + serviceName + " && echo 'RESTART_OK:systemd'; "
                + "elif command -v supervisorctl >/dev/null 2>&1 && supervisorctl status " + serviceName + " >/dev/null 2>&1; then "
                + "supervisorctl restart " + serviceName + " && echo 'RESTART_OK:supervisor'; "
                + "elif [ -f " + deployPath + "/current/start.sh ]; then "
                + "sh " + deployPath + "/current/start.sh restart && echo 'RESTART_OK:start_script' || echo 'RESTART_FAILED:start_script'; "
                + "elif [ -f " + deployPath + "/start.sh ]; then "
                + "sh " + deployPath + "/start.sh restart && echo 'RESTART_OK:start_script' || echo 'RESTART_FAILED:start_script'; "
                + "else echo 'RESTART_NO_MANAGER'; fi";
        return sshUtil.executeCommand(server.getHostname(), server.getPort(),
                server.getUsername(), server.getDecryptedPassword(), server.getDecryptedPrivateKey(),
                server.getPrivateKeyPassphrase(), restartCmd, 60);
    }

    private List<DeployCheckItemDTO> performHealthCheck(DeployRecord record, List<DeployServerDTO> servers) {
        List<DeployCheckItemDTO> checks = new ArrayList<>();
        Project project = projectRepository.selectById(record.getProjectId());
        log.info("========== 健康检查开始 | 项目={} 环境={} 服务器数={} ==========",
                project != null ? project.getName() : "unknown",
                record.getEnv(),
                servers.size());

        for (DeployServerDTO serverDto : servers) {
            Server server = serverRepository.selectById(serverDto.getId());
            if (server == null) continue;

            ProjectServer binding = projectServerRepository.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProjectServer>()
                            .eq(ProjectServer::getProjectId, record.getProjectId())
                            .eq(ProjectServer::getServerId, server.getId())
                            .eq(ProjectServer::getEnv, record.getEnv())
                            .eq(ProjectServer::getDeleted, false));

            if (binding == null) continue;

            String deployPath = binding.getDeployPath();
            int port = guessAppPort(project, deployPath);

            DeployCheckItemDTO httpCheck = new DeployCheckItemDTO();
            httpCheck.setKey("health_http_" + server.getHostname());
            httpCheck.setName("HTTP探活 [" + server.getHostname() + ":" + port + "]");
            String healthUrl = guessHealthUrl(project, server.getHostname(), port);
            if (healthUrl != null) {
                boolean httpOk = doHttpHealthCheck(healthUrl, 15);
                httpCheck.setStatus(httpOk ? "PASS" : "WARNING");
                httpCheck.setMessage(httpOk ? "HTTP响应正常" : "HTTP探活失败（服务可能仍在启动）");
            } else {
                httpCheck.setStatus("WARNING");
                httpCheck.setMessage("未配置健康检查路径");
            }
            checks.add(httpCheck);
        }

        return checks;
    }

    private DeployCheckItemDTO check(String key, String status, String message) {
        DeployCheckItemDTO dto = new DeployCheckItemDTO();
        dto.setKey(key);
        dto.setName(key);
        dto.setStatus(status);
        dto.setMessage(message);
        return dto;
    }

    private String appendLog(String existing, String line) {
        return (existing != null ? existing : "") + line + "\n";
    }

    private String appendStep(String existing, String server, String step) {
        String banner = "┌─────────────────────────────────────────────────────────────\n"
                + "│  " + step + "\n"
                + "│  server: " + server + "\n"
                + "└─────────────────────────────────────────────────────────────";
        log.info("\n{}", banner);
        return appendLog(existing, banner);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<Map<String, Object>> parseArtifacts(String artifactsJson) {
        if (!StringUtils.hasText(artifactsJson)) {
            return null;
        }
        try {
            return objectMapper.readValue(artifactsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (Exception e) {
            log.error("解析构建产物失败: {}", artifactsJson, e);
            return null;
        }
    }

    private String guessArtifactBaseUrl() {
        if (StringUtils.hasText(artifactBaseUrl)) {
            return artifactBaseUrl;
        }
        // Detect the first non-loopback IPv4 address so remote servers can reach us.
        // Configure drp.deploy.artifact-base-url in application.yml to avoid this heuristic.
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String detected = "http://" + addr.getHostAddress() + ":" + serverPort;
                        log.warn("drp.deploy.artifact-base-url 未配置，自动检测到本机地址: {}（建议在配置文件中显式设置）", detected);
                        return detected;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("自动检测本机IP失败", e);
        }
        log.error("无法自动检测本机IP，且未配置 drp.deploy.artifact-base-url，制品下载地址将无法访问");
        return "http://localhost:" + serverPort;
    }

    private String displayStrategy(String strategy) {
        return switch (strategy) {
            case "SINGLE" -> "单机部署";
            case "ROLLING" -> "滚动发布";
            case "BLUE_GREEN" -> "蓝绿部署";
            case "GRAY" -> "灰度发布";
            default -> strategy;
        };
    }

    private String detectCurrentColor(Server server, String deployPath) {
        String target = sshUtil.readSymlink(server.getHostname(), server.getPort(),
                server.getUsername(), server.getDecryptedPassword(),
                server.getDecryptedPrivateKey(), server.getPrivateKeyPassphrase(),
                deployPath + "/current");
        if (target != null) {
            if (target.contains("/blue") || target.endsWith("blue")) return "blue";
            if (target.contains("/green") || target.endsWith("green")) return "green";
        }
        return "blue";
    }

    private int guessAppPort(Project project, String deployPath) {
        return project.getType() != null && project.getType().contains("JAVA") ? 8080 : 80;
    }

    private String guessHealthUrl(Project project, String hostname, int port) {
        String path = "/health";
        if (project.getType() != null && project.getType().contains("JAVA")) {
            path = "/actuator/health";
        }
        return "http://" + hostname + ":" + port + path;
    }

    private boolean doHttpHealthCheck(String url, int timeoutSeconds) {
        int maxRetries = 5;
        for (int n = 1; n <= maxRetries; n++) {
            long start = System.currentTimeMillis();
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(timeoutSeconds))
                        .header("User-Agent", "DRP-Deploy-HealthCheck/1.0")
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                long elapsed = System.currentTimeMillis() - start;
                int statusCode = response.statusCode();
                String body = response.body();
                if (body != null && body.length() > 200) {
                    body = body.substring(0, 200) + "...(truncated)";
                }
                log.info("HTTP健康检查 | 第{}次 | URL={} | 状态码={} | 耗时={}ms | 响应体={}",
                        n, url, statusCode, elapsed, body);
                if (statusCode >= 200 && statusCode < 500) {
                    return true;
                }
                log.warn("HTTP健康检查 | 第{}次 | 状态码异常={}，{}秒后重试",
                        n, statusCode, 2 * n + 1);
            } catch (java.net.http.HttpConnectTimeoutException e) {
                log.warn("HTTP健康检查 | 第{}次 | 连接超时 | URL={} | {}秒后重试",
                        n, url, 2 * n + 1);
            } catch (java.net.http.HttpTimeoutException e) {
                log.warn("HTTP健康检查 | 第{}次 | 响应超时 | URL={} | {}秒后重试",
                        n, url, 2 * n + 1);
            } catch (Exception e) {
                log.warn("HTTP健康检查 | 第{}次 | 异常={} | URL={} | {}秒后重试",
                        n, e.getMessage(), url, 2 * n + 1);
            }

            if (n < maxRetries) {
                try {
                    Thread.sleep((2L * n + 1) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("HTTP健康检查重试被中断 | URL={}", url);
                    return false;
                }
            }
        }
        log.error("HTTP健康检查 | 全部{}次重试失败 | URL={}", maxRetries, url);
        return false;
    }
}
