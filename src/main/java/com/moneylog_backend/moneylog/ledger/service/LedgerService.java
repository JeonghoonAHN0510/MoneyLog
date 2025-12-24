package com.moneylog_backend.moneylog.ledger.service;

import com.moneylog_backend.moneylog.ledger.mapper.LedgerMapper;
import com.moneylog_backend.moneylog.ledger.repository.LedgerRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private final LedgerRepository ledgerRepository;
    private final LedgerMapper ledgerMapper;
} // class end