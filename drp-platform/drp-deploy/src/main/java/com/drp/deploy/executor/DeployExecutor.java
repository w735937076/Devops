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
        String log = record.getLogContent();

        log = appendStep(log, server.getHostname(), "====== " + stepName + " 开始 ======");
        deployRecordRepository.updateById(record);

        log = appendStep(log, server.getHostname(), "步骤 1/5 » 检查目标目录");
        record.setLogContent(log);
        deployRecordRepository.updateById(record);
        sshUtil.executeCommand(server.getHostname(), server.getPort(), server.getUsername(),
                server.getDecryptedPassword(), server.getDecryptedPrivateKey(), server.getPrivateKeyPassphrase(),
                "mkdir -p " + deployPath + " && echo 'DIR_OK'");

        log = appendStep(log, server.getHostname(), "步骤 2/5 » 备份旧版本");
        record.setLogContent(log);
        deployRecordRepository.updateById(record);
        if (!rollbackMode) {
            String backupCmd = String.format(
                    "if [ -f %s/current ]; then " +
                            "BACKUP_DIR=%s/backup_$(date +%%Y%%m%%d_%%H%%M%%S); " +
                            "mkdir -p $BACKUP_DIR; " +
                            "cp -r %s/current/* $BACKUP_DIR/ 2>/dev/null; " +
                            "echo 'BACKUP_OK:$BACKUP_DIR'; " +
                            "else echo 'BACKUP_SKIP'; fi",
                    deployPath, deployPath, deployPath);
            String backupResult = sshUtil.executeCommand(server.getHostname(), server.getPort(),
                    server.getUsername(), server.getDecryptedPassword(), server.getDecryptedPrivateKey(),
                    server.getPrivateKeyPassphrase(), backupCmd, 120);
            log = appendLog(log, "  备份结果: " + backupResult.trim());
            deployRecordRepository.updateById(record);
        }

        log = appendStep(log, server.getHostname(), "步骤 3/5 » 上传制品");
        record.setLogContent(log);
        deployRecordRepository.updateById(record);

        String uploadsDir = deployPath + "/uploads";

        for (Map<String, Object> artifact : artifacts) {
            String artifactName = (String) artifact.get("name");
            Object pathObj = artifact.get("path");
            Object downloadUrlObj = artifact.get("downloadUrl");
            if (pathObj == null && downloadUrlObj == null) continue;

            String targetName = artifactName != null ? artifactName : "app";

            if (pathObj != null) {
                // Local file path — push directly via SFTP (no HTTP server exposure needed)
                java.io.File localFile = new java.io.File(pathObj.toString());
                if (!localFile.exists() || !localFile.isFile()) {
                    throw new BusinessException("制品文件不存在: " + pathObj);
                }
                log = appendLog(log, "  SFTP上传: " + localFile.getName() + " (" + localFile.length() / 1024 + " KB) → " + uploadsDir + "/" + targetName);
                record.setLogContent(log);
                deployRecordRepository.updateById(record);
                sshUtil.uploadFile(server.getHostname(), server.getPort(),
                        server.getUsername(), server.getDecryptedPassword(),
                        server.getDecryptedPrivateKey(), server.getPrivateKeyPassphrase(),
                        localFile.getAbsolutePath(), uploadsDir, targetName);
                log = appendLog(log, "  SFTP上传完成 ✓ " + targetName);
            } else {
                // Explicit external download URL — let the remote server pull it via curl
                String downloadUrl = downloadUrlObj.toString();
                if (!downloadUrl.startsWith("http://") && !downloadUrl.startsWith("https://") && !downloadUrl.startsWith("ftp://")) {
                    downloadUrl = guessArtifactBaseUrl() + downloadUrl;
                }
                log = appendLog(log, "  制品URL下载: " + downloadUrl);
                record.setLogContent(log);
                deployRecordRepository.updateById(record);
                String dlResult = sshUtil.downloadArtifact(
                        server.getHostname(), server.getPort(),
                        server.getUsername(), server.getDecryptedPassword(),
                        server.getDecryptedPrivateKey(), server.getPrivateKeyPassphrase(),
                        downloadUrl, uploadsDir, targetName);
                log = appendLog(log, "  URL下载完成 ✓ " + dlResult.trim());
            }
            deployRecordRepository.updateById(record);
        }

        log = appendStep(log, server.getHostname(), "步骤 4/5 » 切换版本 (" + displayStrategy(strategy) + ")");
        record.setLogContent(log);
        deployRecordRepository.updateById(record);
        String switchResult = executeStrategySwitch(server, deployPath, strategy, rollbackMode, project);
        log = appendLog(log, "  策略切换结果: " + switchResult.trim());
        record.setLogContent(log);
        deployRecordRepository.updateById(record);

        log = appendStep(log, server.getHostname(), "步骤 5/5 » 重启服务");
        record.setLogContent(log);
        deployRecordRepository.updateById(record);
        String restartResult = restartService(server, project, deployPath);
        log = appendLog(log, "  服务重启结果: " + restartResult.trim());

        log = appendStep(log, server.getHostname(), "====== " + stepName + " 完成 ======");
        record.setLogContent(log);
        deployRecordRepository.updateById(record);
    }

    private String executeStrategySwitch(Server server, String deployPath, String strategy, boolean rollbackMode, Project project) {
        String appName = project.getName();
        String strategyCmd;
        if ("BLUE_GREEN".equals(strategy)) {
            String color = detectCurrentColor(deployPath);
            String newColor = "blue".equals(color) ? "green" : "blue";
            strategyCmd = String.format(
                    "cd %s && " +
                            "mkdir -p %s/%s && " +
                            "cp -r %s/uploads/* %s/%s/ && " +
                            "ln -snf %s/%s current && " +
                            "echo 'BLUE_GREEN_SWITCH:%s'",
                    deployPath, deployPath, newColor, deployPath, deployPath, newColor, deployPath, newColor, newColor);
        } else if ("GRAY".equals(strategy)) {
            strategyCmd = String.format(
                    "cd %s && " +
                            "mkdir -p %s/gray && " +
                            "cp -r %s/uploads/* %s/gray/ && " +
                            "ln -snf %s/gray current && " +
                            "echo 'GRAY_SWITCH:gray'",
                    deployPath, deployPath, deployPath, deployPath, deployPath);
        } else {
            strategyCmd = String.format(
                    "cd %s && " +
                            "cp -r %s/uploads/* %s/current/ 2>/dev/null || mkdir -p %s/current && " +
                            "cp -r %s/uploads/* %s/current/ && " +
                            "echo 'SINGLE_SWITCH:OK'",
                    deployPath, deployPath, deployPath, deployPath, deployPath, deployPath);
        }
        return sshUtil.executeCommand(server.getHostname(), server.getPort(),
                server.getUsername(), server.getDecryptedPassword(), server.getDecryptedPrivateKey(),
                server.getPrivateKeyPassphrase(), strategyCmd, 60);
    }

    private String restartService(Server server, Project project, String deployPath) {
        String serviceName = project.getName().replace("-", "").toLowerCase();
        // Source login profiles so JAVA_HOME / PATH are available (JSch exec runs a non-login shell)
        String sourceEnv = ". /etc/profile 2>/dev/null; [ -f ~/.bash_profile ] && . ~/.bash_profile 2>/dev/null; [ -f ~/.bashrc ] && . ~/.bashrc 2>/dev/null; ";
        // Use ';' (not '&&') between stop and start so that stop failing (not running) never blocks start
        // Do NOT redirect start output to /dev/null — keep it visible for diagnosis
        String restartCmd = String.format(
                sourceEnv +
                "if systemctl cat %s >/dev/null 2>&1; then " +
                        "systemctl stop %s 2>/dev/null; systemctl start %s && echo 'RESTART_OK:systemd'; " +
                "elif command -v supervisorctl >/dev/null 2>&1 && supervisorctl status %s >/dev/null 2>&1; then " +
                        "supervisorctl stop %s 2>/dev/null; supervisorctl start %s && echo 'RESTART_OK:supervisor'; " +
                "elif [ -f %s/current/start.sh ]; then " +
                        "sh %s/current/start.sh stop 2>/dev/null; sh %s/current/start.sh start && echo 'RESTART_OK:start_script' || echo 'RESTART_FAILED:start_script'; " +
                "elif [ -f %s/start.sh ]; then " +
                        "sh %s/start.sh stop 2>/dev/null; sh %s/start.sh start && echo 'RESTART_OK:start_script' || echo 'RESTART_FAILED:start_script'; " +
                "else echo 'RESTART_NO_MANAGER'; fi",
                serviceName, serviceName, serviceName,
                serviceName, serviceName, serviceName,
                deployPath, deployPath, deployPath,
                deployPath, deployPath, deployPath);
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

    private String detectCurrentColor(String deployPath) {
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
