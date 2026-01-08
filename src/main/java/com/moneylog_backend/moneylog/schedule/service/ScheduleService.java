package com.moneylog_backend.moneylog.schedule.service;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moneylog_backend.moneylog.schedule.entity.JobMetaEntity;
import com.moneylog_backend.moneylog.schedule.log.LogCleanupJob;
import com.moneylog_backend.moneylog.schedule.repository.ScheduleRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final Scheduler scheduler;

    @PostConstruct
    public void init () {
        try {
            scheduler.clear();

            scheduleRepository.findAll().forEach(meta -> {
                if (meta.is_active()) {
                    registerJob(meta);
                }
            });
        } catch (SchedulerException e) {
            log.error("Scheduler init failed", e);
        }
    }

    public void registerJob (JobMetaEntity meta) {
        try {
            // 실행할 Job 클래스 매핑 (여기서는 로그 삭제 Job으로 고정 예시)
            // 실제로는 Class.forName(meta.getClassName()) 등으로 동적 로딩 가능
            JobDetail jobDetail = JobBuilder.newJob(LogCleanupJob.class)
                                            .withIdentity(meta.getJob_name(), meta.getJob_group())
                                            .build();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                                                .withIdentity(meta.getJob_name() + "_trigger", meta.getJob_group())
                                                .withSchedule(
                                                    CronScheduleBuilder.cronSchedule(meta.getCron_expression()))
                                                .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Job Registered: {} at {}", meta.getJob_name(), meta.getCron_expression());

        } catch (SchedulerException e) {
            log.error("Failed to register job", e);
        }
    }

    @Transactional
    public void updateSchedule (String jobName, String newCron) {
        try {
            JobMetaEntity meta = scheduleRepository.findById(jobName)
                                                   .orElseThrow(() -> new RuntimeException("Job not found"));

            meta.setCron_expression(newCron);
            scheduleRepository.save(meta);

            TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "_trigger", meta.getJob_group());

            CronTrigger newTrigger = TriggerBuilder.newTrigger()
                                                   .withIdentity(triggerKey)
                                                   .withSchedule(CronScheduleBuilder.cronSchedule(newCron))
                                                   .build();

            scheduler.rescheduleJob(triggerKey, newTrigger);

            log.info(">>> Rescheduled Job: {} to {}", jobName, newCron);

        } catch (SchedulerException e) {
            log.error("Failed to reschedule job", e);
            throw new RuntimeException("Reschedule failed");
        }
    }
}
