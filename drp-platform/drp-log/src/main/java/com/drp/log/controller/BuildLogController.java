package com.drp.log.controller;

import com.drp.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs/builds")
@RequiredArgsConstructor
@Slf4j
public class BuildLogController {

    private final StringRedisTemplate redisTemplate;

    private static final String BUILD_LOG_KEY_PREFIX = "build:log:";

    @GetMapping("/{buildId}")
    @PreAuthorize("isAuthenticated()")
    public Result<String> getBuildLog(
            @PathVariable Long buildId,
            @RequestParam(required = false) Long lineFrom,
            @RequestParam(required = false, defaultValue = "1000") int lineLimit) {

        String logKey = BUILD_LOG_KEY_PREFIX + buildId;
        String fullLog = redisTemplate.opsForValue().get(logKey);

        if (fullLog == null || fullLog.isEmpty()) {
            return Result.success("操作成功", "");
        }

        String[] lines = fullLog.split("\n");
        int totalLines = lines.length;

        int from = (lineFrom != null && lineFrom > 0) ? lineFrom.intValue() : Math.max(0, totalLines - lineLimit);
        int to = Math.min(from + lineLimit, totalLines);

        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) {
            sb.append(lines[i]);
            if (i < to - 1) {
                sb.append("\n");
            }
        }

        return Result.success("操作成功", sb.toString());
    }

    @GetMapping("/{buildId}/stream")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> isLogStreamActive(@PathVariable Long buildId) {
        String logKey = BUILD_LOG_KEY_PREFIX + buildId;
        Boolean hasKey = redisTemplate.hasKey(logKey);
        return Result.success("操作成功", hasKey != null && hasKey);
    }
}
