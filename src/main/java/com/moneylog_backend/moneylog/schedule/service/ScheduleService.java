package com.moneylog_backend.moneylog.schedule.service;

import java.util.List;
import java.util.stream.Collectors;

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

import com.moneylog_backend.moneylog.schedule.dto.ScheduleResDto;
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

    public List<ScheduleResDto> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(ScheduleResDto::from)
                .collect(Collectors.toList());
    }

    @PostConstruct
    public void init () {
        try {
            scheduler.clear();

            scheduleRepository.findAll().forEach(meta -> {
                if (meta.isActive()) {
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
                                            .withIdentity(meta.getJobName(), meta.getJobGroup())
                                            .build();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                                                .withIdentity(meta.getJobName() + "_trigger", meta.getJobGroup())
                                                .withSchedule(
                                                    CronScheduleBuilder.cronSchedule(meta.getCronExpression()))
                                                .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Job Registered: {} at {}", meta.getJobName(), meta.getCronExpression());

        } catch (SchedulerException e) {
            log.error("Failed to register job", e);
        }
    }

    @Transactional
    public void updateSchedule (com.moneylog_backend.moneylog.schedule.dto.ScheduleReqDto reqDto) {
        try {
            String jobName = reqDto.getJobName();
            JobMetaEntity meta = scheduleRepository.findById(jobName)
                                                   .orElseThrow(() -> new RuntimeException("Job not found: " + jobName));

            String newCron = generateCronExpression(reqDto);
            
            // 변경 사항이 없으면 skip
            if (newCron.equals(meta.getCronExpression())) {
                log.info(">>> Schedule is same, skip update: {}", jobName);
                return;
            }

            meta.updateCron(newCron);
            scheduleRepository.save(meta);

            TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "_trigger", meta.getJobGroup());

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

    private String generateCronExpression(com.moneylog_backend.moneylog.schedule.dto.ScheduleReqDto dto) {
        // time format: HH:mm
        String[] parts = dto.getTime().split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        switch (dto.getFrequency()) {
            case "DAILY": // 매일 HH:mm:00
                return String.format("0 %d %d * * ?", minute, hour);
            
            case "WEEKLY": // 매주 요일 HH:mm:00
                if (dto.getDayOfWeek() == null) {
                    throw new IllegalArgumentException("WEEKLY requires dayOfWeek (1-7)");
                }
                // Quartz: 1=SUN, 2=MON, ..., 7=SAT
                // Input: 1=MON, ..., 7=SUN
                // Mapping: 1->2, ..., 6->7, 7->1
                int quartzDay = (dto.getDayOfWeek() % 7) + 1;
                return String.format("0 %d %d ? * %d", minute, hour, quartzDay);

            case "MONTHLY": // 매월 일 HH:mm:00
                if (dto.getDayOfMonth() == null) {
                    throw new IllegalArgumentException("MONTHLY requires dayOfMonth (1-31)");
                }
                return String.format("0 %d %d %d * ?", minute, hour, dto.getDayOfMonth());

            default:
                throw new IllegalArgumentException("Unknown frequency: " + dto.getFrequency());
        }
    }
}
