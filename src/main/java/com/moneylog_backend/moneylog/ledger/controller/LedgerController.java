package com.moneylog_backend.moneylog.ledger.controller;

import com.moneylog_backend.global.auth.annotation.LoginUser;
import com.moneylog_backend.moneylog.ledger.dto.LedgerDto;
import com.moneylog_backend.moneylog.ledger.service.LedgerService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {
    private final LedgerService ledgerService;

    @PostMapping
    public ResponseEntity<?> saveLedger (@RequestBody LedgerDto ledgerDto, @LoginUser Integer user_id) {
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

    @GetMapping
    public ResponseEntity<?> getLedgersByUserId (@LoginUser Integer user_id) {
        if (user_id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(ledgerService.getLedgersByUserId(user_id));
    }

    // todo 검색 조건에 따른 가계부 조회 API 필요

    @PutMapping
    public ResponseEntity<?> updateLedger(@RequestBody LedgerDto ledgerDto, @LoginUser Integer user_id) {
        if (ledgerDto == null || user_id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        ledgerDto.setUser_id(user_id);
        return ResponseEntity.ok(ledgerService.updateLedger(ledgerDto));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteLedger (@RequestParam Integer ledger_id, @LoginUser Integer user_id) {
        if (ledger_id == null || user_id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        LedgerDto ledgerDto = LedgerDto.builder().ledger_id(ledger_id).user_id(user_id).build();

        return ResponseEntity.ok(ledgerService.deleteLedger(ledgerDto));
    }
}