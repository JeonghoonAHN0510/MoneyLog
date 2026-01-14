package com.moneylog_backend.moneylog.loan.service;

import com.moneylog_backend.moneylog.loan.mapper.LoanMapper;
import com.moneylog_backend.moneylog.loan.repository.LoanRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
}