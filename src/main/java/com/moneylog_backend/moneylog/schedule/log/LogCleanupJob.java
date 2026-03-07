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
        log.info("Quartz 로그 정리 작업을 시작합니다.");

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        try {
            logRepository.deleteLogsOlderThan(thirtyDaysAgo);
            log.info("{} 이전 로그를 정리했습니다.", thirtyDaysAgo);
        } catch (Exception e) {
            log.error("Quartz 로그 정리 작업에 실패했습니다.", e);

        }
    }
}
