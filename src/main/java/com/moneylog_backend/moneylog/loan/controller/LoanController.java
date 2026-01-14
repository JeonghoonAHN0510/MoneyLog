package com.moneylog_backend.moneylog.loan.controller;

import com.moneylog_backend.moneylog.loan.service.LoanService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loan")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;
}