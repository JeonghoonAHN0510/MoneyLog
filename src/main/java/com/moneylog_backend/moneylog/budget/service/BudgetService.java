package com.moneylog_backend.moneylog.budget.service;

import com.moneylog_backend.moneylog.budget.mapper.BudgetMapper;
import com.moneylog_backend.moneylog.budget.repository.BudgetRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;
}