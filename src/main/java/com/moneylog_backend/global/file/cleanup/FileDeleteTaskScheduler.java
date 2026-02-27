package com.moneylog_backend.global.file.cleanup;

import com.moneylog_backend.global.file.FileProperties;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileDeleteTaskScheduler {
    private final FileDeleteTaskService fileDeleteTaskService;
    private final FileProperties fileProperties;

    @Scheduled(fixedDelayString = "${app.file.cleanup.fixed-delay-ms:60000}")
    public void processPendingDeleteTasks() {
        if (!fileProperties.getCleanup().isEnabled()) {
            return;
        }

        fileDeleteTaskService.processPendingTasks();
    }
}
