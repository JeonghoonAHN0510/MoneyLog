package com.moneylog_backend.moneylog.schedule.dto;

import com.moneylog_backend.moneylog.schedule.entity.JobMetaEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleResDto {
    private String jobName;
    private String jobGroup;
    private String cronExpression;
    private String description;
    private boolean isActive;

    public static ScheduleResDto from(JobMetaEntity entity) {
        return ScheduleResDto.builder()
                .jobName(entity.getJobName())
                .jobGroup(entity.getJobGroup())
                .cronExpression(entity.getCronExpression())
                .description(entity.getDescription())
                .isActive(entity.isActive())
                .build();
    }
}
