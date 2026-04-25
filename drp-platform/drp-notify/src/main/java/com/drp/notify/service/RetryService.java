package com.drp.notify.service;

public interface RetryService {
    void scheduleRetry(Long recordId, int currentRetry);
}
