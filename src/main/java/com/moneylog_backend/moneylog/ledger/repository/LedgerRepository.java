package com.moneylog_backend.moneylog.ledger.repository;

import com.moneylog_backend.moneylog.ledger.entity.LedgerEntity;

import org.springframework.data.repository.CrudRepository;

public interface LedgerRepository extends CrudRepository<LedgerEntity, Integer> {
} // interface end