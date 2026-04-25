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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FeishuChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(FeishuChannel.class);

    private final RestTemplate restTemplate;

    public FeishuChannel(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public NotificationChannelType getChannelType() {
        return NotificationChannelType.FEISHU;
    }

    @Override
    public String getChannelName() {
        return "飞书";
    }

    @Override
    public SendResult send(NotifyMessage message, String configStr) {
        JSONObject config = JSON.parseObject(configStr);
        String webhookUrl = config.getString("webhookUrl");

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("msg_type", "interactive");
            body.put("card", buildCard(message));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            FeishuResponse response = restTemplate.postForEntity(webhookUrl, request, FeishuResponse.class).getBody();

            if (response != null && "success".equals(response.getCode())) {
                String messageId = response.getData() != null ? response.getData().getMessageId() : null;
                return SendResult.success(messageId);
            } else {
                return SendResult.fail("发送失败: " + response);
            }
        } catch (Exception e) {
            log.error("飞书发送失败", e);
            return SendResult.fail("发送异常: " + e.getMessage());
        }
    }

    @Override
    public boolean test(String configStr) {
        try {
            NotifyMessage testMsg = NotifyMessage.builder()
                    .title("飞书测试通知")
                    .content("这是一条测试消息\n\n如果您收到此消息，说明飞书通知配置成功。")
                    .severity("INFO")
                    .build();
            return send(testMsg, configStr).isSuccess();
        } catch (Exception e) {
            log.error("飞书测试失败", e);
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
                "placeholder", "https://open.feishu.cn/open-apis/bot/v2/hook/xxx"
        ));
        return JSON.toJSONString(schema);
    }

    private Map<String, Object> buildCard(NotifyMessage message) {
        Map<String, Object> card = new HashMap<>();
        card.put("header", Map.of(
                "title", Map.of("tag", "plain_text", "content", message.getTitle()),
                "template", getTemplateColor(message.getSeverity())
        ));
        card.put("elements", buildElements(message));
        return card;
    }

    private List<Map<String, Object>> buildElements(NotifyMessage message) {
        List<Map<String, Object>> elements = new ArrayList<>();
        elements.add(Map.of("tag", "markdown", "content", message.getContent()));
        elements.add(Map.of("tag", "hr"));

        List<Map<String, Object>> fields = new ArrayList<>();
        if (message.getProjectName() != null) {
            fields.add(Map.of("is_short", true, "text",
                    Map.of("tag", "lark_md", "content", "**项目**: " + message.getProjectName())));
        }
        if (message.getEnv() != null) {
            fields.add(Map.of("is_short", true, "text",
                    Map.of("tag", "lark_md", "content", "**环境**: " + message.getEnv())));
        }
        if (message.getVersion() != null) {
            fields.add(Map.of("is_short", true, "text",
                    Map.of("tag", "lark_md", "content", "**版本**: " + message.getVersion())));
        }
        if (message.getOperator() != null) {
            fields.add(Map.of("is_short", true, "text",
                    Map.of("tag", "lark_md", "content", "**操作人**: " + message.getOperator())));
        }

        if (!fields.isEmpty()) {
            elements.add(Map.of("tag", "div", "fields", fields));
        }

        elements.add(Map.of("tag", "note",
                "elements", List.of(Map.of("tag", "plain_text", "content",
                        "发送时间: " + LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))));

        return elements;
    }

    private String getTemplateColor(String severity) {
        if (severity == null) return "blue";
        return switch (severity) {
            case "WARNING" -> "orange";
            case "CRITICAL" -> "red";
            default -> "blue";
        };
    }

    private static class FeishuResponse {
        private String code;
        private String msg;
        private Data data;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMsg() { return msg; }
        public void setMsg(String msg) { this.msg = msg; }
        public Data getData() { return data; }
        public void setData(Data data) { this.data = data; }

        private static class Data {
            private String messageId;
            public String getMessageId() { return messageId; }
            public void setMessageId(String messageId) { this.messageId = messageId; }
        }
    }
}
