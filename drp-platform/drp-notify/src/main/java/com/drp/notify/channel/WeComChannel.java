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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WeComChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(WeComChannel.class);

    private final RestTemplate restTemplate;

    public WeComChannel(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public NotificationChannelType getChannelType() {
        return NotificationChannelType.WECOM;
    }

    @Override
    public String getChannelName() {
        return "企业微信";
    }

    @Override
    public SendResult send(NotifyMessage message, String configStr) {
        JSONObject config = JSON.parseObject(configStr);
        String webhookUrl = config.getString("webhookUrl");

        Map<String, Object> body = new HashMap<>();
        body.put("msgtype", "markdown");

        Map<String, Object> markdown = new HashMap<>();
        markdown.put("content", buildMarkdownContent(message));
        body.put("markdown", markdown);

        if (config.containsKey("safe")) {
            body.put("safe", config.getInteger("safe"));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            WeComResponse response = restTemplate.postForEntity(webhookUrl, request, WeComResponse.class).getBody();

            if (response != null && response.getErrCode() == 0) {
                return SendResult.success(response.getMsgId());
            } else {
                String errMsg = response != null ? response.getErrMsg() : "未知错误";
                return SendResult.fail("发送失败: " + errMsg);
            }
        } catch (Exception e) {
            log.error("企业微信发送失败", e);
            return SendResult.fail("发送异常: " + e.getMessage());
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
            log.error("企业微信测试失败", e);
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
                "placeholder", "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx"
        ));
        schema.put("safe", Map.of(
                "type", "select",
                "label", "安全设置",
                "options", List.of(
                        Map.of("label", "不加密", "value", 0),
                        Map.of("label", "签名校验", "value", 1),
                        Map.of("label", "IP地址", "value", 2),
                        Map.of("label", "签名+IP", "value", 4)
                ),
                "default", 0
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

    private String buildMarkdownContent(NotifyMessage message) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(getSeverityEmoji(message.getSeverity()))
                .append(" ").append(message.getTitle()).append("\n\n");
        sb.append(message.getContent()).append("\n\n");
        sb.append("---\n\n");

        if (message.getProjectName() != null) {
            sb.append("- **项目**: ").append(message.getProjectName()).append("\n");
        }
        if (message.getEnv() != null) {
            sb.append("- **环境**: ").append(formatEnv(message.getEnv())).append("\n");
        }
        if (message.getVersion() != null) {
            sb.append("- **版本**: ").append(message.getVersion()).append("\n");
        }
        if (message.getOperator() != null) {
            sb.append("- **操作人**: ").append(message.getOperator()).append("\n");
        }
        sb.append("- **时间**: ").append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");

        return sb.toString();
    }

    private String buildTestContent() {
        return "这是一条测试消息\n\n" +
                "如果您收到此消息，说明企业微信通知配置成功。\n\n" +
                "- **状态**: ✅ 正常\n" +
                "- **时间**: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static class WeComResponse {
        private Integer errCode;
        private String errMsg;
        private String msgId;

        public Integer getErrCode() { return errCode; }
        public void setErrCode(Integer errCode) { this.errCode = errCode; }
        public String getErrMsg() { return errMsg; }
        public void setErrMsg(String errMsg) { this.errMsg = errMsg; }
        public String getMsgId() { return msgId; }
        public void setMsgId(String msgId) { this.msgId = msgId; }
    }
}
