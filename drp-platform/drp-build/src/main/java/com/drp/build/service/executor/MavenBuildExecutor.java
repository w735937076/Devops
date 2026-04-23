package com.drp.build.service.executor;

import com.drp.build.entity.BuildRecord;
import com.drp.build.entity.Pipeline;
import com.drp.build.enums.BuildStatus;
import com.drp.build.service.BuildService;
import com.drp.build.util.OsUtil;
import com.drp.project.entity.Project;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
public class MavenBuildExecutor implements BuildExecutor {

    private static final Logger log = LoggerFactory.getLogger(MavenBuildExecutor.class);
    private static final int BUILD_TIMEOUT_SECONDS = 3600;
    private static final String WORKSPACE_BASE = OsUtil.isWindows() ? "D:\\DrpArtifact" : "/tmp/drp-builds";

    private final BuildService buildService;
    private final ObjectMapper objectMapper;

    public MavenBuildExecutor(@Lazy BuildService buildService, ObjectMapper objectMapper) {
        this.buildService = buildService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return "JAVA_MAVEN";
    }

    @Override
    @Async
    public void executeAsync(BuildRecord record, Project project, Pipeline pipeline) {
        try {
            executeSync(record, project, pipeline);
        } catch (Exception e) {
            log.error("异步构建执行失败", e);
            buildService.finishBuild(record.getId(), BuildStatus.FAILED.getCode(), e.getMessage());
        }
    }

    @Override
    public BuildResult executeSync(BuildRecord record, Project project, Pipeline pipeline) {
        record.setStatus(BuildStatus.RUNNING.getCode());
        record.setStartTime(java.time.LocalDateTime.now());

        String workspace = String.format("%s/%d/%d", WORKSPACE_BASE, record.getProjectId(), record.getId());

        try {
            // 2. Git Clone
            buildService.pushBuildLog(record.getId(), "[Checkout] 开始克隆代码...\n");
            String gitUrl = project.getGitUrl();
            String cloneCmd = String.format("git clone -b %s --depth 1 %s \"%s\"",
                    record.getBranch(), gitUrl, workspace);

            ProcessBuilder cloneBuilder = new ProcessBuilder(OsUtil.getShell(), OsUtil.getShellArg(), cloneCmd);
            cloneBuilder.environment().put("GIT_TERMINAL_PROMPT", "0");
            cloneBuilder.redirectErrorStream(true);

            Process cloneProcess = cloneBuilder.start();
            String cloneOutput = readStream(cloneProcess.getInputStream());
            boolean cloneFinished = cloneProcess.waitFor(300, TimeUnit.SECONDS);

            if (!cloneFinished || cloneProcess.exitValue() != 0) {
                buildService.pushBuildLog(record.getId(), "[Error] Git clone failed: " + cloneOutput + "\n");
                buildService.finishBuild(record.getId(), BuildStatus.FAILED.getCode(), "Git clone failed");
                return new BuildResult(false, cloneOutput, "Git clone failed", -1);
            }

            buildService.pushBuildLog(record.getId(), "[Checkout] 代码克隆完成\n");

            // 3. Maven Build
            buildService.pushBuildLog(record.getId(), "[Build] 开始构建...\n");

            String mavenGoals = extractMavenGoals(pipeline);
            String buildCmd = String.format("%s && mvn %s -DskipTests", OsUtil.getCdCommand(workspace), mavenGoals);

            ProcessBuilder buildBuilder = new ProcessBuilder(OsUtil.getShell(), OsUtil.getShellArg(), buildCmd);
            buildBuilder.redirectErrorStream(true);

            Process buildProcess = buildBuilder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(buildProcess.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    buildService.pushBuildLog(record.getId(), line + "\n");
                }
            }

            boolean buildFinished = buildProcess.waitFor(BUILD_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!buildFinished) {
                buildProcess.destroyForcibly();
                buildService.pushBuildLog(record.getId(), "[Error] Build timeout\n");
                buildService.finishBuild(record.getId(), BuildStatus.TIMEOUT.getCode(), "Build timeout");
                return new BuildResult(false, "", "Build timeout", -1);
            }

            int exitCode = buildProcess.exitValue();

            if (exitCode == 0) {
                buildService.pushBuildLog(record.getId(), "[Success] 构建成功!\n");
                String artifacts = buildService.generateArtifacts(project, record, workspace);
                buildService.finishBuild(record.getId(), BuildStatus.SUCCESS.getCode(), null, artifacts);
                return new BuildResult(true, "Build success", null, 0);
            } else {
                buildService.pushBuildLog(record.getId(), "[Error] 构建失败，退出码: " + exitCode + "\n");
                buildService.finishBuild(record.getId(), BuildStatus.FAILED.getCode(), "Build failed with exit code " + exitCode);
                return new BuildResult(false, "", "Build failed", exitCode);
            }

        } catch (Exception e) {
            log.error("构建执行异常", e);
            buildService.pushBuildLog(record.getId(), "[Error] " + e.getMessage() + "\n");
            buildService.finishBuild(record.getId(), BuildStatus.FAILED.getCode(), e.getMessage());
            return new BuildResult(false, "", e.getMessage(), -1);
        }
    }

    private String extractMavenGoals(Pipeline pipeline) {
        try {
            JsonNode stages = objectMapper.readTree(pipeline.getStages());
            for (JsonNode stage : stages) {
                if ("MAVEN_BUILD".equals(stage.get("type").asText())) {
                    JsonNode config = stage.get("config");
                    if (config != null && config.has("goals")) {
                        return config.get("goals").asText();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析流水线配置失败", e);
        }
        return "clean package";
    }

    private String readStream(java.io.InputStream inputStream) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
