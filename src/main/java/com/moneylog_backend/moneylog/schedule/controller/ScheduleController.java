package com.moneylog_backend.moneylog.schedule.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moneylog_backend.moneylog.schedule.service.ScheduleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    // todo cron으로 받지말고, RequestVO를 만들어서, Service에서 cron식 생성
    @PostMapping("/update")
    public String updateSchedule (@RequestParam String jobName, @RequestParam String cron) {
        scheduleService.updateSchedule(jobName, cron);
        return "스케줄이 [" + cron + "] 으로 변경되었습니다.";
    }
}
