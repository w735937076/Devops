package com.drp.notify.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drp.notify.dto.NotificationTemplateDTO;
import com.drp.notify.entity.NotificationTemplate;
import com.drp.notify.repository.NotificationTemplateRepository;
import com.drp.common.dto.PageResponse;
import com.drp.common.result.Result;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notify/templates")
public class NotificationTemplateController {

    private final NotificationTemplateRepository templateRepository;

    public NotificationTemplateController(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @GetMapping
    public Result<PageResponse<NotificationTemplateDTO>> getTemplates(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String event,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<NotificationTemplate> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();

        if (channel != null && !channel.isBlank()) {
            wrapper.eq(NotificationTemplate::getChannel, channel);
        }
        if (event != null && !event.isBlank()) {
            wrapper.eq(NotificationTemplate::getEvent, event);
        }

        wrapper.orderByDesc(NotificationTemplate::getCreateTime);
        Page<NotificationTemplate> result = templateRepository.selectPage(pageParam, wrapper);

        List<NotificationTemplateDTO> records = result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return Result.success(PageResponse.of(records, result.getTotal(), page, size));
    }

    @GetMapping("/{id}")
    public Result<NotificationTemplateDTO> getTemplate(@PathVariable Long id) {
        NotificationTemplate template = templateRepository.selectById(id);
        return Result.success(toDTO(template));
    }

    @PostMapping
    @Transactional
    public Result<Void> createTemplate(@RequestBody NotificationTemplate template) {
        templateRepository.insert(template);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @Transactional
    public Result<Void> updateTemplate(@PathVariable Long id, @RequestBody NotificationTemplate template) {
        template.setId(id);
        templateRepository.updateById(template);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        templateRepository.deleteById(id);
        return Result.success("删除成功");
    }

    private NotificationTemplateDTO toDTO(NotificationTemplate template) {
        if (template == null) return null;
        NotificationTemplateDTO dto = new NotificationTemplateDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setChannel(template.getChannel());
        dto.setEvent(template.getEvent());
        dto.setTitleTemplate(template.getTitleTemplate());
        dto.setContentTemplate(template.getContentTemplate());
        dto.setVariables(template.getVariables());
        dto.setEnabled(template.getEnabled());
        return dto;
    }
}
