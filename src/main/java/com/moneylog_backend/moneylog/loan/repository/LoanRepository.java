package com.moneylog_backend.moneylog.loan.repository;

import com.moneylog_backend.moneylog.loan.entity.LoanEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<LoanEntity, Integer> {
} // interface end