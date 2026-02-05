package com.moneylog_backend.moneylog.transaction.repository;

import com.moneylog_backend.moneylog.transaction.entity.TransactionEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Integer> {}