package com.moneylog_backend.moneylog.schedule.event;

import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.moneylog_backend.moneylog.schedule.service.ScheduleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleQuartzSyncListener {
    private final ScheduleService scheduleService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle (ScheduleQuartzSyncRequestedEvent event) {
        try {
            scheduleService.synchronizeQuartzJob(event.jobName());
        } catch (SchedulerException | RuntimeException e) {
            log.error("커밋 후 Quartz 스케줄 동기화에 실패했습니다. jobName={}", event.jobName(), e);
        }
    }
}
