package com.moneylog_backend.moneylog.budget.repository;

import com.moneylog_backend.moneylog.budget.entity.BudgetEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Integer> {
} // interface end