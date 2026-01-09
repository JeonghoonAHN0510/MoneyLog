package com.moneylog_backend.moneylog.bank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moneylog_backend.moneylog.bank.service.BankService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class BankController {
    private final BankService bankService;

    @GetMapping
    public ResponseEntity<?> getBankList () {
        return ResponseEntity.ok(bankService.getBankList());
    }
}
