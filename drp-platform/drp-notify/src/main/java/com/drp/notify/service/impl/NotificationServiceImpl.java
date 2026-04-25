package com.drp.notify.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drp.common.dto.PageResponse;
import com.drp.notify.channel.NotificationChannel;
import com.drp.notify.channel.NotificationChannelFactory;
import com.drp.notify.dto.*;
import com.drp.notify.entity.NotificationConfig;
import com.drp.notify.entity.NotificationRecord;
import com.drp.notify.entity.NotificationTemplate;
import com.drp.notify.enums.NotificationChannelType;
import com.drp.notify.enums.NotificationStatus;
import com.drp.notify.repository.NotificationConfigRepository;
import com.drp.notify.repository.NotificationRecordRepository;
import com.drp.notify.repository.NotificationTemplateRepository;
import com.drp.notify.service.NotificationService;
import com.drp.notify.service.RateLimitService;
import com.drp.notify.service.RetryService;
import com.drp.project.entity.Project;
import com.drp.project.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationConfigRepository configRepository;
    private final NotificationRecordRepository recordRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationChannelFactory channelFactory;
    private final RateLimitService rateLimitService;
    private final RetryService retryService;
    private final ProjectRepository projectRepository;

    public NotificationServiceImpl(NotificationConfigRepository configRepository,
                                   NotificationRecordRepository recordRepository,
                                   NotificationTemplateRepository templateRepository,
                                   NotificationChannelFactory channelFactory,
                                   RateLimitService rateLimitService,
                                   RetryService retryService,
                                   ProjectRepository projectRepository) {
        this.configRepository = configRepository;
        this.recordRepository = recordRepository;
        this.templateRepository = templateRepository;
        this.channelFactory = channelFactory;
        this.rateLimitService = rateLimitService;
        this.retryService = retryService;
        this.projectRepository = projectRepository;
    }

    @Override
    @Async("notifyExecutor")
    @Transactional
    public void sendNotification(NotifyEvent event) {
        log.info("发送通知 | event: {} | projectId: {}", event.getEvent(), event.getProjectId());

        List<NotificationConfig> configs = findConfigs(event);
        if (configs.isEmpty()) {
            log.info("没有匹配的通知配置 | event: {}", event.getEvent());
            return;
        }

        for (NotificationConfig config : configs) {
            if (!Boolean.TRUE.equals(config.getEnabled())) {
                continue;
            }

            if (!rateLimitService.shouldSend(event, config.getChannel(), config.getRecipients())) {
                log.info("通知被频率控制抑制 | event: {} | channel: {} | recipient: {}",
                        event.getEvent(), config.getChannel(), config.getRecipients());
                continue;
            }

            NotificationChannel channel = channelFactory.getChannel(config.getChannel());
            if (channel == null) {
                log.warn("未找到通知渠道: {}", config.getChannel());
                continue;
            }

            NotifyMessage message = buildMessage(event, config);

            String configJson = config.getConfig() != null ? JSON.toJSONString(config.getConfig()) : "{}";
            SendResult result = channel.send(message, configJson);

            saveRecord(config, message, result);

            rateLimitService.recordSend(event, config.getChannel(), config.getRecipients());

            if (!result.isSuccess()) {
                NotificationRecord record = recordRepository.findLatestByConfigIdAndRecipient(
                        config.getId(), message.getRecipient()).orElse(null);
                if (record != null) {
                    retryService.scheduleRetry(record.getId(), 0);
                }
            }
        }
    }

    private List<NotificationConfig> findConfigs(NotifyEvent event) {
        List<NotificationConfig> configs = new ArrayList<>();

        if (event.getProjectId() != null) {
            configs.addAll(configRepository.findByProjectIdAndEventAndEnabled(
                    event.getProjectId(), event.getEvent(), true));
        }

        configs.addAll(configRepository.findByProjectIdIsNullAndEventAndEnabled(
                event.getEvent(), true));

        return configs;
    }

    private NotifyMessage buildMessage(NotifyEvent event, NotificationConfig config) {
        String title = event.getTitle();
        String content = event.getContent();

        if (config.getTemplateId() != null) {
            NotificationTemplate template = templateRepository.selectById(config.getTemplateId());
            if (template != null) {
                title = renderTemplate(template.getTitleTemplate(), event);
                content = renderTemplate(template.getContentTemplate(), event);
            }
        }

        return NotifyMessage.builder()
                .title(title)
                .content(content)
                .projectName(event.getProjectName())
                .env(event.getEnv())
                .version(event.getVersion())
                .operator(event.getOperator())
                .severity(event.getSeverity())
                .recipient(config.getRecipients())
                .build();
    }

    private String renderTemplate(String templateStr, NotifyEvent event) {
        String result = templateStr;
        result = result.replace("{{projectName}}", nvl(event.getProjectName()));
        result = result.replace("{{env}}", nvl(event.getEnv()));
        result = result.replace("{{version}}", nvl(event.getVersion()));
        result = result.replace("{{operator}}", nvl(event.getOperator()));
        result = result.replace("{{content}}", nvl(event.getContent()));
        result = result.replace("{{time}}", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return result;
    }

    private String nvl(String str) {
        return str != null ? str : "-";
    }

    private void saveRecord(NotificationConfig config, NotifyMessage message, SendResult result) {
        NotificationRecord record = new NotificationRecord();
        record.setConfigId(config.getId());
        record.setChannel(config.getChannel());
        record.setEvent(config.getEvent());
        record.setRecipient(message.getRecipient());
        record.setTitle(message.getTitle());
        record.setContent(message.getContent());
        record.setStatus(result.isSuccess() ? NotificationStatus.SUCCESS.name() : NotificationStatus.FAIL.name());
        record.setErrorMessage(result.getErrorMessage());
        record.setExternalId(result.getExternalId());
        record.setSendTime(LocalDateTime.now());
        record.setSentTime(result.isSuccess() ? LocalDateTime.now() : null);
        record.setRetryCount(0);
        record.setMaxRetry(3);
        recordRepository.insert(record);
    }

    public PageResponse<NotificationRecordDTO> queryRecords(String channel, String status, int page, int size) {
        Page<NotificationRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<NotificationRecord> wrapper = new LambdaQueryWrapper<>();

        if (channel != null && !channel.isBlank()) {
            wrapper.eq(NotificationRecord::getChannel, channel);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(NotificationRecord::getStatus, status);
        }

        wrapper.orderByDesc(NotificationRecord::getSendTime);
        Page<NotificationRecord> result = recordRepository.selectPage(pageParam, wrapper);

        List<NotificationRecordDTO> records = result.getRecords().stream()
                .map(this::toRecordDTO)
                .toList();

        return PageResponse.of(records, result.getTotal(), page, size);
    }

    private NotificationRecordDTO toRecordDTO(NotificationRecord record) {
        NotificationRecordDTO dto = new NotificationRecordDTO();
        dto.setId(record.getId());
        dto.setConfigId(record.getConfigId());
        dto.setChannel(record.getChannel());
        dto.setEvent(record.getEvent());
        dto.setRecipient(record.getRecipient());
        dto.setTitle(record.getTitle());
        dto.setContent(record.getContent());
        dto.setStatus(record.getStatus());
        dto.setStatusText(NotificationStatus.valueOf(record.getStatus()).getDescription());
        dto.setRetryCount(record.getRetryCount());
        dto.setMaxRetry(record.getMaxRetry());
        dto.setErrorMessage(record.getErrorMessage());
        dto.setSendTime(record.getSendTime() != null ? record.getSendTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        dto.setSentTime(record.getSentTime() != null ? record.getSentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        dto.setExternalId(record.getExternalId());
        return dto;
    }

    public NotificationRecordDTO getRecordById(Long id) {
        NotificationRecord record = recordRepository.selectById(id);
        return record != null ? toRecordDTO(record) : null;
    }

    public void retryRecord(Long recordId) {
        NotificationRecord record = recordRepository.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("记录不存在");
        }

        NotificationConfig config = configRepository.selectById(record.getConfigId());
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }

        NotificationChannel channel = channelFactory.getChannel(config.getChannel());
        if (channel == null) {
            throw new RuntimeException("渠道不存在");
        }

        NotifyMessage message = NotifyMessage.builder()
                .title(record.getTitle())
                .content(record.getContent())
                .recipient(record.getRecipient())
                .build();

        String retryConfigJson = config.getConfig() != null ? JSON.toJSONString(config.getConfig()) : "{}";
        SendResult result = channel.send(message, retryConfigJson);

        record.setRetryCount(record.getRetryCount() != null ? record.getRetryCount() + 1 : 1);
        if (result.isSuccess()) {
            record.setStatus(NotificationStatus.SUCCESS.name());
            record.setSentTime(LocalDateTime.now());
            record.setExternalId(result.getExternalId());
        } else {
            record.setErrorMessage(result.getErrorMessage());
            if (record.getRetryCount() >= record.getMaxRetry()) {
                record.setStatus(NotificationStatus.FAIL.name());
            } else {
                record.setStatus(NotificationStatus.RETRY.name());
                retryService.scheduleRetry(record.getId(), record.getRetryCount());
            }
        }
        recordRepository.updateById(record);
    }
}
