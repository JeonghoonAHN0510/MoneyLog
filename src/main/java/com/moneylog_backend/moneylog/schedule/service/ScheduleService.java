package com.moneylog_backend.moneylog.schedule.service;

import java.util.List;
import java.util.stream.Collectors;

import org.quartz.JobKey;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.type.ScheduleEnum;
import com.moneylog_backend.moneylog.schedule.dto.ScheduleReqDto;
import com.moneylog_backend.moneylog.schedule.dto.ScheduleResDto;
import com.moneylog_backend.moneylog.schedule.entity.JobMetaEntity;
import com.moneylog_backend.moneylog.schedule.event.ScheduleQuartzSyncRequestedEvent;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    public List<ScheduleResDto> getAllSchedules () {
        return scheduleRepository.findAll().stream().map(ScheduleResDto::from).collect(Collectors.toList());
    }

    @PostConstruct
    public void init () {
        try {
            scheduler.clear();

            scheduleRepository.findAll().forEach(meta -> {
                try {
                    reconcileQuartzJob(meta);
                } catch (SchedulerException e) {
                    log.error("스케줄러 초기화 중 작업 동기화에 실패했습니다. jobName={}", meta.getJobName(), e);
                }
            });
        } catch (SchedulerException e) {
            log.error("스케줄러 초기화에 실패했습니다.", e);
        }
    }

    @Transactional
    public void updateSchedule (ScheduleReqDto reqDto) {
        String jobName = reqDto.getJobName();
        JobMetaEntity meta = scheduleRepository.findById(jobName)
                                               .orElseThrow(
                                                   () -> new RuntimeException(
                                                       ErrorMessageConstants.scheduleJobNotFound(jobName)));

        String newCron = generateCronExpression(reqDto);

        if (newCron.equals(meta.getCronExpression())) {
            log.info("변경된 스케줄은 없지만 Quartz 정합성 복구를 위해 동기화를 요청합니다. jobName={}", jobName);
        } else {
            meta.updateCron(newCron);
            scheduleRepository.saveAndFlush(meta);
            log.info("스케줄 메타데이터를 갱신했습니다. jobName={}, cronExpression={}", jobName, newCron);
        }

        applicationEventPublisher.publishEvent(new ScheduleQuartzSyncRequestedEvent(jobName));
    }

    public void synchronizeQuartzJob (String jobName) throws SchedulerException {
        JobMetaEntity meta = scheduleRepository.findById(jobName)
                                               .orElseThrow(
                                                   () -> new RuntimeException(
                                                       ErrorMessageConstants.scheduleJobNotFound(jobName)));
        reconcileQuartzJob(meta);
    }

    private void reconcileQuartzJob (JobMetaEntity meta) throws SchedulerException {
        TriggerKey triggerKey = triggerKey(meta);
        JobKey jobKey = jobKey(meta);

        if (!meta.isActive()) {
            scheduler.unscheduleJob(triggerKey);
            scheduler.deleteJob(jobKey);
            log.info("비활성 스케줄을 Quartz에서 제거했습니다. jobName={}", meta.getJobName());
            return;
        }

        CronTrigger currentTrigger = scheduler.getTrigger(triggerKey) instanceof CronTrigger cronTrigger
            ? cronTrigger
            : null;
        boolean jobExists = scheduler.checkExists(jobKey);

        if (!jobExists || currentTrigger == null) {
            if (currentTrigger != null) {
                scheduler.unscheduleJob(triggerKey);
            }
            if (jobExists) {
                scheduler.deleteJob(jobKey);
            }
            scheduleJob(meta);
            log.info("Quartz 스케줄을 새로 등록했습니다. jobName={}, cronExpression={}", meta.getJobName(), meta.getCronExpression());
            return;
        }

        if (meta.getCronExpression().equals(currentTrigger.getCronExpression())) {
            log.info("Quartz 스케줄이 이미 최신 상태입니다. jobName={}, cronExpression={}", meta.getJobName(), meta.getCronExpression());
            return;
        }

        CronTrigger newTrigger = buildTrigger(meta);
        scheduler.rescheduleJob(triggerKey, newTrigger);
        log.info("Quartz 스케줄을 DB 기준으로 재동기화했습니다. jobName={}, cronExpression={}", meta.getJobName(), meta.getCronExpression());
    }

    private void scheduleJob (JobMetaEntity meta) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(LogCleanupJob.class)
                                        .withIdentity(jobKey(meta))
                                        .build();

        scheduler.scheduleJob(jobDetail, buildTrigger(meta));
    }

    private CronTrigger buildTrigger (JobMetaEntity meta) {
        return TriggerBuilder.newTrigger()
                             .withIdentity(triggerKey(meta))
                             .withSchedule(CronScheduleBuilder.cronSchedule(meta.getCronExpression()))
                             .build();
    }

    private JobKey jobKey (JobMetaEntity meta) {
        return JobKey.jobKey(meta.getJobName(), meta.getJobGroup());
    }

    private TriggerKey triggerKey (JobMetaEntity meta) {
        return TriggerKey.triggerKey(meta.getJobName() + "_trigger", meta.getJobGroup());
    }

    private String generateCronExpression (ScheduleReqDto dto) {
        // time format: HH:mm
        String[] parts = dto.getTime().split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        // 1. 문자열을 Enum 객체로 변환
        ScheduleEnum schedule = ScheduleEnum.fromString(dto.getFrequency());

        // 2. 다형성을 이용해 Cron 생성 (분, 시, 요일, 일 전달)
        return schedule.toCron(minute, hour, dto.getDayOfWeek(), dto.getDayOfMonth());
    }
}
