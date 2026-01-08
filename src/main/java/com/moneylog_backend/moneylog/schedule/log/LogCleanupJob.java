package com.moneylog_backend.moneylog.schedule.log;

import java.time.LocalDateTime;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import com.moneylog_backend.global.log.repository.LogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogCleanupJob extends QuartzJobBean {
    private final LogRepository logRepository;

    @Override
    protected void executeInternal (JobExecutionContext context) throws JobExecutionException {
        log.info(">>> Quartz Job Started: Cleaning up old logs...");

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        try {
            logRepository.deleteLogsOlderThan(thirtyDaysAgo);
            log.info(">>> Quartz Job Finished: Logs older than {} have been deleted.", thirtyDaysAgo);
        } catch (Exception e) {
            log.error(">>> Quartz Job Failed: ", e);

        }
    }
}
