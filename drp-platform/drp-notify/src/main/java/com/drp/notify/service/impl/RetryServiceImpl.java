package com.drp.notify.service.impl;

import com.drp.notify.service.RetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class RetryServiceImpl implements RetryService {

    private static final Logger log = LoggerFactory.getLogger(RetryServiceImpl.class);

    private static final int MAX_RETRY = 3;
    private static final long[] RETRY_DELAYS = {30, 60, 300};

    private final NotificationServiceImpl notificationService;

    public RetryServiceImpl(@Lazy NotificationServiceImpl notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    @Async("notifyRetryExecutor")
    public void scheduleRetry(Long recordId, int currentRetry) {
        if (currentRetry >= MAX_RETRY) {
            log.info("达到最大重试次数，不再重试 | recordId: {}", recordId);
            return;
        }

        long delay = RETRY_DELAYS[Math.min(currentRetry, RETRY_DELAYS.length - 1)];

        log.info("安排重试 | recordId: {} | delay: {}s | retry: {}", recordId, delay, currentRetry + 1);

        CompletableFuture.delayedExecutor(delay, TimeUnit.SECONDS)
                .execute(() -> {
                    try {
                        notificationService.retryRecord(recordId);
                    } catch (Exception e) {
                        log.error("重试失败 | recordId: {}", recordId, e);
                    }
                });
    }
}
