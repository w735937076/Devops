package com.drp.notify.config;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(MailProperties properties) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(properties.getHost());

        Integer port = properties.getPort();
        sender.setPort(port != null ? port : 465);

        sender.setUsername(properties.getUsername());
        sender.setPassword(properties.getPassword());
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", properties.getProperties().getOrDefault("mail.smtp.ssl.enable", "true"));
        props.put("mail.smtp.timeout", "5000");

        return sender;
    }
}
