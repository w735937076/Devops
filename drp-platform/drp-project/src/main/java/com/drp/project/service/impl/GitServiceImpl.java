package com.drp.project.service.impl;

import com.drp.common.exception.BusinessException;
import com.drp.project.entity.Credential;
import com.drp.project.repository.CredentialRepository;
import com.drp.project.service.CredentialService;
import com.drp.project.service.GitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Git 仓库服务实现
 *
 * @author Nick
 */
@Service
public class GitServiceImpl implements GitService {

    private static final Logger log = LoggerFactory.getLogger(GitServiceImpl.class);
    private static final int TIMEOUT_SECONDS = 12;
    private static final Pattern BRANCH_PATTERN = Pattern.compile("^[a-f0-9]+\\s+refs/heads/(.+)$");

    private final CredentialService credentialService;
    private final CredentialRepository credentialRepository;

    public GitServiceImpl(CredentialService credentialService, CredentialRepository credentialRepository) {
        this.credentialService = credentialService;
        this.credentialRepository = credentialRepository;
    }

    @Override
    public List<String> fetchBranches(String gitUrl, Long credentialId) {
        if (!StringUtils.hasText(gitUrl)) {
            throw new BusinessException("无效的Git仓库地址");
        }

        String normalizedUrl = normalizeGitUrl(gitUrl);
        String host = extractHost(normalizedUrl);

        // 如果提供了凭证，构建带认证的 URL
        String authenticatedUrl = normalizedUrl;
        if (credentialId != null) {
            authenticatedUrl = buildAuthenticatedUrl(normalizedUrl, credentialId, host);
        }

        log.info("Fetching branches from host: {}", host);

        try {
            List<String> branches = executeGitLsRemote(authenticatedUrl, host);
            log.info("Successfully fetched {} branches from host: {}", branches.size(), host);
            return branches;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch branches from host: {}", host, e);
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                throw new BusinessException("连接仓库超时，请检查网络或仓库地址");
            }
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("authentication")) {
                throw new BusinessException("认证失败，请检查凭证是否有效");
            }
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")) {
                throw new BusinessException("仓库不存在或地址错误");
            }
            throw new BusinessException("获取分支列表失败: " + e.getMessage());
        }
    }

    private String normalizeGitUrl(String url) {
        url = url.trim();
        // 移除末尾的 .git 后缀（如果存在）
        if (url.endsWith(".git")) {
            url = url.substring(0, url.length() - 4);
        }
        // 确保以 https:// 开头
        if (!url.startsWith("https://")) {
            // 尝试自动修正 http -> https
            if (url.startsWith("http://")) {
                url = "https://" + url.substring(7);
            } else {
                throw new BusinessException("无效的Git仓库地址，仅支持 HTTPS 协议");
            }
        }
        return url;
    }

    private String extractHost(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getHost();
        } catch (Exception e) {
            throw new BusinessException("无效的Git仓库地址");
        }
    }

    private String buildAuthenticatedUrl(String baseUrl, Long credentialId, String host) {
        Credential credential = credentialRepository.selectById(credentialId);
        if (credential == null) {
            throw new BusinessException("关联的凭证不存在");
        }

        String secretContent = credentialService.getDecryptedContent(credentialId);

        switch (credential.getType()) {
            case "USERNAME_PASSWORD":
                // username:password@host/repo
                if (!StringUtils.hasText(credential.getAccount())) {
                    throw new BusinessException("用户名密码类型凭证缺少账号信息");
                }
                return baseUrl.replace("https://", "https://" + encodeUriComponent(credential.getAccount())
                        + ":" + encodeUriComponent(secretContent) + "@");

            case "ACCESS_TOKEN":
                // token@host/repo (token as username)
                return baseUrl.replace("https://", "https://" + encodeUriComponent(secretContent) + "@");

            case "SSH_KEY":
                throw new BusinessException("SSH密钥暂不支持，请使用用户名密码或访问令牌");

            default:
                throw new BusinessException("不支持的凭证类型: " + credential.getType());
        }
    }

    private String encodeUriComponent(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private List<String> executeGitLsRemote(String url, String host) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "git", "ls-remote", "--heads", "--quiet", url
        );
        pb.environment().put("GIT_TERMINAL_PROMPT", "0");
        pb.redirectErrorStream(true);

        Process process = pb.start();

        List<String> branches = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    Matcher matcher = BRANCH_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        branches.add(matcher.group(1));
                    }
                }
            }
        }

        boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("timeout");
        }

        if (process.exitValue() != 0) {
            throw new RuntimeException("git command failed with exit code: " + process.exitValue());
        }

        return branches;
    }
}
