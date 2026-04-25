package com.drp.notify.channel;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.drp.notify.dto.NotifyMessage;
import com.drp.notify.dto.SendResult;
import com.drp.notify.enums.NotificationChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EmailChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(EmailChannel.class);

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public EmailChannel(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public NotificationChannelType getChannelType() {
        return NotificationChannelType.EMAIL;
    }

    @Override
    public String getChannelName() {
        return "邮件";
    }

    @Override
    public SendResult send(NotifyMessage message, String configStr) {
        if (mailProperties.getHost() == null || mailProperties.getHost().isBlank()) {
            return SendResult.fail("邮件服务未配置");
        }

        JSONObject config = JSON.parseObject(configStr);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String from = config.getString("from");
            helper.setFrom(from != null ? from : config.getString("username"));
            helper.setTo(message.getRecipient().split(","));
            helper.setSubject(message.getTitle());
            helper.setText(buildHtmlContent(message), true);

            mailSender.send(mimeMessage);
            return SendResult.success(mimeMessage.getMessageID());

        } catch (Exception e) {
            log.error("邮件发送失败", e);
            return SendResult.fail("发送异常: " + e.getMessage());
        }
    }

    @Override
    public boolean test(String configStr) {
        try {
            NotifyMessage testMsg = NotifyMessage.builder()
                    .title("DRP 平台通知测试")
                    .content("这是一封测试邮件，如果您收到此邮件，说明邮件通知配置成功。")
                    .severity("INFO")
                    .recipient(JSON.parseObject(configStr).getString("testRecipient"))
                    .build();
            return send(testMsg, configStr).isSuccess();
        } catch (Exception e) {
            log.error("邮件测试失败", e);
            return false;
        }
    }

    @Override
    public String getConfigSchema() {
        return """
                {
                    "smtpHost": {"type": "string", "label": "SMTP服务器", "required": true},
                    "smtpPort": {"type": "number", "label": "端口", "required": true, "default": 465},
                    "username": {"type": "string", "label": "用户名", "required": true},
                    "password": {"type": "password", "label": "密码", "required": true},
                    "from": {"type": "string", "label": "发件人", "required": false},
                    "useSsl": {"type": "boolean", "label": "使用SSL", "required": false, "default": true},
                    "testRecipient": {"type": "string", "label": "测试收件人", "required": true}
                }
                """;
    }

    private String getSeverityColor(String severity) {
        if (severity == null) return "#1890ff";
        return switch (severity) {
            case "WARNING" -> "#faad14";
            case "CRITICAL" -> "#ff4d4f";
            default -> "#1890ff";
        };
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String buildHtmlContent(NotifyMessage message) {
        StringBuilder metaHtml = new StringBuilder();
        if (message.getProjectName() != null) {
            metaHtml.append(String.format("<div class='meta-item'><span class='meta-label'>项目</span><span>%s</span></div>",
                    escapeHtml(message.getProjectName())));
        }
        if (message.getEnv() != null) {
            metaHtml.append(String.format("<div class='meta-item'><span class='meta-label'>环境</span><span>%s</span></div>",
                    escapeHtml(message.getEnv())));
        }
        if (message.getVersion() != null) {
            metaHtml.append(String.format("<div class='meta-item'><span class='meta-label'>版本</span><span>%s</span></div>",
                    escapeHtml(message.getVersion())));
        }
        if (message.getOperator() != null) {
            metaHtml.append(String.format("<div class='meta-item'><span class='meta-label'>操作人</span><span>%s</span></div>",
                    escapeHtml(message.getOperator())));
        }

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header {
                            background: %s;
                            color: white;
                            padding: 20px;
                            border-radius: 8px 8px 0 0;
                        }
                        .header h2 { margin: 0; }
                        .content { background: #f5f5f5; padding: 20px; border-radius: 0 0 8px 8px; }
                        .meta { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; }
                        .meta-item { display: flex; margin: 8px 0; }
                        .meta-label { font-weight: bold; width: 80px; }
                        .footer { margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>%s</h2>
                        </div>
                        <div class="content">
                            <p>%s</p>
                            <div class="meta">
                                %s
                            </div>
                        </div>
                        <div class="footer">
                            <p>此邮件由 DRP 平台自动发送，请勿回复。</p>
                            <p>发送时间: %s</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                getSeverityColor(message.getSeverity()),
                escapeHtml(message.getTitle()),
                escapeHtml(message.getContent()).replace("\n", "<br>"),
                metaHtml.toString(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
}
