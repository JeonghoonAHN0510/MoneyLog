package com.moneylog_backend.moneylog.ledger.repository;

import com.moneylog_backend.moneylog.ledger.entity.LedgerEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRepository extends JpaRepository<LedgerEntity, Integer> {
} // interface end