package com.moneylog_backend.moneylog.schedule.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.moneylog_backend.moneylog.schedule.dto.ScheduleReqDto;
import com.moneylog_backend.moneylog.schedule.entity.JobMetaEntity;
import com.moneylog_backend.moneylog.schedule.event.ScheduleQuartzSyncRequestedEvent;
import com.moneylog_backend.moneylog.schedule.repository.ScheduleRepository;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {
    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private Scheduler scheduler;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ScheduleService scheduleService;

    @Captor
    private ArgumentCaptor<Object> eventCaptor;
    @Captor
    private ArgumentCaptor<CronTrigger> cronTriggerCaptor;

    @Mock
    private CronTrigger currentTrigger;

    @Test
    void 스케줄_수정_성공시_DB_저장_후_동기화_이벤트를_발행한다() {
        JobMetaEntity meta = activeMeta("0 0 3 * * ?");
        when(scheduleRepository.findById("logCleanupJob")).thenReturn(Optional.of(meta));

        scheduleService.updateSchedule(dailyRequest("04:30"));

        verify(scheduleRepository).saveAndFlush(meta);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        ScheduleQuartzSyncRequestedEvent event =
            assertInstanceOf(ScheduleQuartzSyncRequestedEvent.class, eventCaptor.getValue());
        assertEquals("logCleanupJob", event.jobName());
        verifyNoInteractions(scheduler);
    }

    @Test
    void 같은_cron_요청도_drift_복구용_이벤트를_발행한다() {
        JobMetaEntity meta = activeMeta("0 0 3 * * ?");
        when(scheduleRepository.findById("logCleanupJob")).thenReturn(Optional.of(meta));

        scheduleService.updateSchedule(dailyRequest("03:00"));

        verify(scheduleRepository, never()).saveAndFlush(any());
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        ScheduleQuartzSyncRequestedEvent event =
            assertInstanceOf(ScheduleQuartzSyncRequestedEvent.class, eventCaptor.getValue());
        assertEquals("logCleanupJob", event.jobName());
        verifyNoInteractions(scheduler);
    }

    @Test
    void 낙관락_충돌시_Quartz_mutation_없이_예외를_전파한다() {
        JobMetaEntity meta = activeMeta("0 0 3 * * ?");
        when(scheduleRepository.findById("logCleanupJob")).thenReturn(Optional.of(meta));
        when(scheduleRepository.saveAndFlush(meta))
            .thenThrow(new ObjectOptimisticLockingFailureException(JobMetaEntity.class, "logCleanupJob"));

        assertThrows(ObjectOptimisticLockingFailureException.class,
            () -> scheduleService.updateSchedule(dailyRequest("04:30")));

        verifyNoInteractions(applicationEventPublisher, scheduler);
    }

    @Test
    void Quartz_trigger_누락시_새로_등록한다() throws Exception {
        JobMetaEntity meta = activeMeta("0 0 3 * * ?");
        when(scheduleRepository.findById("logCleanupJob")).thenReturn(Optional.of(meta));
        when(scheduler.getTrigger(triggerKey())).thenReturn(null);
        when(scheduler.checkExists(jobKey())).thenReturn(false);

        scheduleService.synchronizeQuartzJob("logCleanupJob");

        verify(scheduler, never()).deleteJob(jobKey());
        verify(scheduler).scheduleJob(any(JobDetail.class), any(CronTrigger.class));
    }

    @Test
    void 같은_cron이면_Quartz를_재등록하지_않는다() throws Exception {
        JobMetaEntity meta = activeMeta("0 0 3 * * ?");
        when(scheduleRepository.findById("logCleanupJob")).thenReturn(Optional.of(meta));
        when(scheduler.getTrigger(triggerKey())).thenReturn(currentTrigger);
        when(scheduler.checkExists(jobKey())).thenReturn(true);
        when(currentTrigger.getCronExpression()).thenReturn("0 0 3 * * ?");

        scheduleService.synchronizeQuartzJob("logCleanupJob");

        verify(scheduler, never()).rescheduleJob(any(), any());
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(CronTrigger.class));
    }

    @Test
    void cron_불일치면_DB_기준으로_Quartz를_재등록한다() throws Exception {
        JobMetaEntity meta = activeMeta("0 30 4 * * ?");
        when(scheduleRepository.findById("logCleanupJob")).thenReturn(Optional.of(meta));
        when(scheduler.getTrigger(triggerKey())).thenReturn(currentTrigger);
        when(scheduler.checkExists(jobKey())).thenReturn(true);
        when(currentTrigger.getCronExpression()).thenReturn("0 0 3 * * ?");

        scheduleService.synchronizeQuartzJob("logCleanupJob");

        verify(scheduler).rescheduleJob(eq(triggerKey()), cronTriggerCaptor.capture());
        assertEquals("0 30 4 * * ?", cronTriggerCaptor.getValue().getCronExpression());
    }

    @Test
    void 비활성_스케줄은_Quartz에서_제거한다() throws Exception {
        JobMetaEntity meta = inactiveMeta("0 0 3 * * ?");
        when(scheduleRepository.findById("logCleanupJob")).thenReturn(Optional.of(meta));

        scheduleService.synchronizeQuartzJob("logCleanupJob");

        verify(scheduler).unscheduleJob(triggerKey());
        verify(scheduler).deleteJob(jobKey());
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(CronTrigger.class));
        verify(scheduler, never()).rescheduleJob(any(), any());
    }

    private ScheduleReqDto dailyRequest(String time) {
        return ScheduleReqDto.builder()
                             .jobName("logCleanupJob")
                             .frequency("DAILY")
                             .time(time)
                             .build();
    }

    private JobMetaEntity activeMeta(String cronExpression) {
        return JobMetaEntity.builder()
                            .jobName("logCleanupJob")
                            .jobGroup("system")
                            .cronExpression(cronExpression)
                            .description("로그 정리")
                            .isActive(true)
                            .version(0L)
                            .build();
    }

    private JobMetaEntity inactiveMeta(String cronExpression) {
        return JobMetaEntity.builder()
                            .jobName("logCleanupJob")
                            .jobGroup("system")
                            .cronExpression(cronExpression)
                            .description("로그 정리")
                            .isActive(false)
                            .version(0L)
                            .build();
    }

    private JobKey jobKey() {
        return JobKey.jobKey("logCleanupJob", "system");
    }

    private TriggerKey triggerKey() {
        return TriggerKey.triggerKey("logCleanupJob_trigger", "system");
    }
}
