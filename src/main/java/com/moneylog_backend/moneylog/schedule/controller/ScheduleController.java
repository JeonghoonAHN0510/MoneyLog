package com.moneylog_backend.moneylog.schedule.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/list")
    public ResponseEntity<List<com.moneylog_backend.moneylog.schedule.dto.ScheduleResDto>> getSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    @PostMapping("/update")
    public String updateSchedule (@RequestBody com.moneylog_backend.moneylog.schedule.dto.ScheduleReqDto reqDto) {
        scheduleService.updateSchedule(reqDto);
        return "스케줄이 변경되었습니다.";
    }
}
