package com.drp.notify.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drp.notify.channel.NotificationChannel;
import com.drp.notify.channel.NotificationChannelFactory;
import com.drp.notify.dto.ChannelConfigRequest;
import com.drp.notify.dto.NotificationConfigDTO;
import com.drp.notify.entity.NotificationConfig;
import com.drp.notify.entity.NotificationTemplate;
import com.drp.notify.enums.NotificationChannelType;
import com.drp.notify.repository.NotificationConfigRepository;
import com.drp.notify.repository.NotificationTemplateRepository;
import com.drp.project.entity.Project;
import com.drp.project.repository.ProjectRepository;
import com.drp.common.dto.PageResponse;
import com.drp.common.result.Result;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notify")
public class NotificationConfigController {

    private final NotificationConfigRepository configRepository;
    private final NotificationTemplateRepository templateRepository;
    private final ProjectRepository projectRepository;
    private final NotificationChannelFactory channelFactory;

    public NotificationConfigController(NotificationConfigRepository configRepository,
                                       NotificationTemplateRepository templateRepository,
                                       ProjectRepository projectRepository,
                                       NotificationChannelFactory channelFactory) {
        this.configRepository = configRepository;
        this.templateRepository = templateRepository;
        this.projectRepository = projectRepository;
        this.channelFactory = channelFactory;
    }

    @GetMapping("/configs")
    public Result<PageResponse<NotificationConfigDTO>> getConfigs(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String channel,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<NotificationConfig> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<NotificationConfig> wrapper = new LambdaQueryWrapper<>();

        if (projectId != null) {
            wrapper.eq(NotificationConfig::getProjectId, projectId);
        }
        if (channel != null && !channel.isBlank()) {
            wrapper.eq(NotificationConfig::getChannel, channel);
        }

        wrapper.orderByDesc(NotificationConfig::getCreateTime);
        Page<NotificationConfig> result = configRepository.selectPage(pageParam, wrapper);

        List<NotificationConfigDTO> records = result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return Result.success(PageResponse.of(records, result.getTotal(), page, size));
    }

    @GetMapping("/configs/{id}")
    public Result<NotificationConfigDTO> getConfig(@PathVariable Long id) {
        NotificationConfig config = configRepository.selectById(id);
        return Result.success(toDTO(config));
    }

    @PostMapping("/configs")
    @Transactional
    public Result<Void> createConfig(@RequestBody NotificationConfig config) {
        configRepository.insert(config);
        return Result.success("创建成功");
    }

    @PutMapping("/configs/{id}")
    @Transactional
    public Result<Void> updateConfig(@PathVariable Long id, @RequestBody NotificationConfig config) {
        config.setId(id);
        configRepository.updateById(config);
        return Result.success("更新成功");
    }

    @DeleteMapping("/configs/{id}")
    @Transactional
    public Result<Void> deleteConfig(@PathVariable Long id) {
        configRepository.deleteById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/test")
    public Result<Boolean> testChannel(@RequestBody ChannelConfigRequest request) {
        NotificationChannel channel = channelFactory.getChannel(request.getChannel());
        if (channel == null) {
            return Result.error("不支持的渠道");
        }

        boolean success = channel.test(JSON.toJSONString(request.getConfig()));
        if (success) {
            return Result.success("测试成功", true);
        } else {
            return Result.error("测试失败");
        }
    }

    @GetMapping("/channels")
    public Result<List<NotificationChannelInfo>> getChannels() {
        List<NotificationChannelInfo> channels = List.of(NotificationChannelType.values()).stream()
                .map(type -> {
                    NotificationChannel channel = channelFactory.getChannel(type);
                    return new NotificationChannelInfo(
                            type.name(),
                            type.getDescription(),
                            channel != null ? channel.getConfigSchema() : null
                    );
                })
                .collect(Collectors.toList());
        return Result.success(channels);
    }

    private NotificationConfigDTO toDTO(NotificationConfig config) {
        NotificationConfigDTO dto = new NotificationConfigDTO();
        dto.setId(config.getId());
        dto.setProjectId(config.getProjectId());
        dto.setChannel(config.getChannel());
        dto.setEvent(config.getEvent());
        dto.setEnabled(config.getEnabled());
        dto.setConfig(config.getConfig());
        dto.setRecipients(config.getRecipients());
        dto.setTemplateId(config.getTemplateId());
        dto.setRateLimit(config.getRateLimit());
        dto.setCreateTime(config.getCreateTime() != null ?
                config.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);

        if (config.getProjectId() != null) {
            Project project = projectRepository.selectById(config.getProjectId());
            if (project != null) {
                dto.setProjectName(project.getName());
            }
        }

        return dto;
    }

    private record NotificationChannelInfo(String code, String name, String configSchema) {}
}
