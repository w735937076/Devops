package com.drp.notify.channel;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.drp.notify.dto.NotifyMessage;
import com.drp.notify.dto.SendResult;
import com.drp.notify.enums.NotificationChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DingTalkChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(DingTalkChannel.class);

    private final RestTemplate restTemplate;

    public DingTalkChannel(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public NotificationChannelType getChannelType() {
        return NotificationChannelType.DINGTALK;
    }

    @Override
    public String getChannelName() {
        return "钉钉";
    }

    @Override
    public SendResult send(NotifyMessage message, String configStr) {
        JSONObject config = JSON.parseObject(configStr);
        String webhookUrl = config.getString("webhookUrl");
        String secret = config.getString("secret");

        try {
            String signUrl = buildSignedUrl(webhookUrl, secret);

            Map<String, Object> body = new HashMap<>();
            body.put("msgtype", "markdown");

            Map<String, Object> markdown = new HashMap<>();
            markdown.put("title", buildTitle(message));
            markdown.put("text", buildMarkdownContent(message));
            body.put("markdown", markdown);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            DingTalkResponse response = restTemplate.postForEntity(signUrl, request, DingTalkResponse.class).getBody();

            if (response != null && response.getErrCode() == 0) {
                return SendResult.success(String.valueOf(response.getTaskId()));
            } else {
                String errMsg = response != null ? response.getErrmsg() : "未知错误";
                return SendResult.fail("发送失败: " + errMsg);
            }
        } catch (Exception e) {
            log.error("钉钉发送失败", e);
            return SendResult.fail("发送异常: " + e.getMessage());
        }
    }

    private String buildSignedUrl(String webhookUrl, String secret) {
        if (secret == null || secret.isBlank()) {
            return webhookUrl;
        }

        try {
            long timestamp = System.currentTimeMillis();
            String stringToSign = timestamp + "\n" + secret;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] signBytes = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));

            String sign = Base64.getEncoder().encodeToString(signBytes);
            sign = URLEncoder.encode(sign, StandardCharsets.UTF_8);

            String separator = webhookUrl.contains("?") ? "&" : "?";
            return webhookUrl + separator + "timestamp=" + timestamp + "&sign=" + sign;

        } catch (Exception e) {
            log.error("钉钉签名失败", e);
            return webhookUrl;
        }
    }

    @Override
    public boolean test(String configStr) {
        try {
            NotifyMessage testMsg = NotifyMessage.builder()
                    .title("测试通知")
                    .content(buildTestContent())
                    .severity("INFO")
                    .build();
            SendResult result = send(testMsg, configStr);
            return result.isSuccess();
        } catch (Exception e) {
            log.error("钉钉测试失败", e);
            return false;
        }
    }

    @Override
    public String getConfigSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("webhookUrl", Map.of(
                "type", "string",
                "label", "Webhook地址",
                "required", true,
                "placeholder", "https://oapi.dingtalk.com/robot/send?access_token=xxx"
        ));
        schema.put("secret", Map.of(
                "type", "string",
                "label", "加签密钥",
                "required", false,
                "placeholder", "安全设置中的加签密钥"
        ));
        return JSON.toJSONString(schema);
    }

    private String getSeverityEmoji(String severity) {
        if (severity == null) return "ℹ️";
        return switch (severity) {
            case "WARNING" -> "⚠️";
            case "CRITICAL" -> "🔴";
            default -> "ℹ️";
        };
    }

    private String formatEnv(String env) {
        if (env == null) return "";
        return switch (env) {
            case "dev" -> "🛠️ 开发环境";
            case "test" -> "🧪 测试环境";
            case "pre" -> "🚀 预发布环境";
            case "prod" -> "🏭 生产环境";
            default -> env;
        };
    }

    private String buildTitle(NotifyMessage message) {
        return getSeverityEmoji(message.getSeverity()) + " " + message.getTitle();
    }

    private String buildMarkdownContent(NotifyMessage message) {
        StringBuilder sb = new StringBuilder();
        sb.append("## ").append(message.getTitle()).append("\n\n");
        sb.append(message.getContent()).append("\n\n");
        sb.append("---\n\n");

        if (message.getProjectName() != null) {
            sb.append("**项目**: ").append(message.getProjectName()).append("\n\n");
        }
        if (message.getEnv() != null) {
            sb.append("**环境**: ").append(formatEnv(message.getEnv())).append("\n\n");
        }
        if (message.getVersion() != null) {
            sb.append("**版本**: ").append(message.getVersion()).append("\n\n");
        }
        if (message.getOperator() != null) {
            sb.append("**操作人**: ").append(message.getOperator()).append("\n\n");
        }
        sb.append("**时间**: ").append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        return sb.toString();
    }

    private String buildTestContent() {
        return "这是一条测试消息\n\n" +
                "如果您收到此消息，说明钉钉通知配置成功。\n\n" +
                "**状态**: ✅ 正常\n\n" +
                "**时间**: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static class DingTalkResponse {
        private Integer errCode;
        private String errmsg;
        private Long taskId;

        public Integer getErrCode() { return errCode; }
        public void setErrCode(Integer errCode) { this.errCode = errCode; }
        public String getErrmsg() { return errmsg; }
        public void setErrmsg(String errmsg) { this.errmsg = errmsg; }
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
    }
}
