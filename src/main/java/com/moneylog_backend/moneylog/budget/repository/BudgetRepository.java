package com.moneylog_backend.moneylog.budget.repository;

import com.moneylog_backend.moneylog.budget.entity.BudgetEntity;

import org.springframework.data.repository.CrudRepository;

public interface BudgetRepository extends CrudRepository<BudgetEntity, Integer> {
} // interface end