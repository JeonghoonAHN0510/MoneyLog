package com.moneylog_backend.moneylog.loan.repository;

import com.moneylog_backend.moneylog.loan.entity.LoanEntity;

import org.springframework.data.repository.CrudRepository;

public interface LoanRepository extends CrudRepository<LoanEntity, Integer> {
} // interface end