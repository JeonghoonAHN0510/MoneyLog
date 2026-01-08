package com.moneylog_backend.global.log.eventListener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.moneylog_backend.global.log.dto.LogDto;
import com.moneylog_backend.global.log.entity.LogEntity;
import com.moneylog_backend.global.log.repository.LogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogEventListener {
    private final LogRepository logRepository;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLogEvent (LogDto logDto) {
        try {
            LogEntity logEntity = logDto.toEntity();
            logRepository.save(logEntity);
        } catch (Exception e) {
            log.error("DB에 Log 기록 실패", e);
        }
    }
}
