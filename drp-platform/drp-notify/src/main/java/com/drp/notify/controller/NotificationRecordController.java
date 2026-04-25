package com.drp.notify.controller;

import com.drp.notify.dto.NotificationRecordDTO;
import com.drp.notify.service.impl.NotificationServiceImpl;
import com.drp.common.dto.PageResponse;
import com.drp.common.result.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notify")
public class NotificationRecordController {

    private final NotificationServiceImpl notificationService;

    public NotificationRecordController(NotificationServiceImpl notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/records")
    public Result<PageResponse<NotificationRecordDTO>> getRecords(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<NotificationRecordDTO> pageResponse = notificationService.queryRecords(channel, status, page, size);
        return Result.success(pageResponse);
    }

    @GetMapping("/records/{id}")
    public Result<NotificationRecordDTO> getRecord(@PathVariable Long id) {
        return Result.success(notificationService.getRecordById(id));
    }

    @PostMapping("/records/{id}/retry")
    public Result<Void> retryRecord(@PathVariable Long id) {
        notificationService.retryRecord(id);
        return Result.success("已加入重试队列");
    }
}
