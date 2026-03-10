package com.moneylog_backend.moneylog.user.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEmailHashBackfillRunner implements ApplicationRunner {
    private final UserEmailHashBackfillService userEmailHashBackfillService;

    @Override
    public void run(ApplicationArguments args) {
        int updatedCount = userEmailHashBackfillService.backfillMissingEmailHashes();
        log.info("email_hash backfill completed. updatedCount={}", updatedCount);
    }
}
