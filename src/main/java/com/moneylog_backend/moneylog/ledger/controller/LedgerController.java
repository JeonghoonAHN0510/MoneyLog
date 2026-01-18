package com.moneylog_backend.moneylog.ledger.controller;

import com.moneylog_backend.global.auth.annotation.LoginUser;
import com.moneylog_backend.moneylog.ledger.dto.LedgerDto;
import com.moneylog_backend.moneylog.ledger.service.LedgerService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {
    private final LedgerService ledgerService;

    @PostMapping
    public ResponseEntity<?> saveLedger(@RequestBody LedgerDto ledgerDto, @LoginUser Integer user_id) {
        if (ledgerDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        ledgerDto.setUser_id(user_id);

        int resultValue = ledgerService.saveLedger(ledgerDto);
        if (resultValue == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            return ResponseEntity.ok(resultValue);
        }
    }
}