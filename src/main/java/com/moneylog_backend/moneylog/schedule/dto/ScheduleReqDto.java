package com.moneylog_backend.moneylog.schedule.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleReqDto {

    @NotBlank(message = "Job Name은 필수입니다")
    private String jobName;

    @NotBlank(message = "빈도 설정은 필수입니다 (DAILY, WEEKLY, MONTHLY)")
    @Pattern(regexp = "^(DAILY|WEEKLY|MONTHLY)$", message = "DAILY, WEEKLY, MONTHLY 중 하나여야 합니다")
    private String frequency;

    @NotBlank(message = "시간은 필수입니다 (HH:mm)")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "HH:mm 형식이어야 합니다")
    private String time;

    @Min(value = 1, message = "요일은 1~7 사이여야 합니다.")
    @Max(value = 7, message = "요일은 1~7 사이여야 합니다.")
    private Integer dayOfWeek; // 1(Mon) ~ 7(Sun)

    @Min(value = 1, message = "실행일은 1~31 사이여야 합니다.")
    @Max(value = 31, message = "실행일은 1~31 사이여야 합니다.")
    private Integer dayOfMonth;
}
