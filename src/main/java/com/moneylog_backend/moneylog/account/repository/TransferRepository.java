package com.moneylog_backend.moneylog.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moneylog_backend.moneylog.ledger.entity.TransferEntity;

public interface TransferRepository extends JpaRepository<TransferEntity, Integer> {}