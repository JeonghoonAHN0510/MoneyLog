package com.moneylog_backend.moneylog.budget.service;

import java.util.List;

import com.moneylog_backend.moneylog.budget.dto.BudgetDto;
import com.moneylog_backend.moneylog.budget.entity.BudgetEntity;
import com.moneylog_backend.moneylog.budget.mapper.BudgetMapper;
import com.moneylog_backend.moneylog.budget.repository.BudgetRepository;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BudgetService {
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;

    @Transactional
    public int saveBudget (BudgetDto budgetDto) {
        int category_id = budgetDto.getCategory_id();
        if (!hasBudget(category_id)) {
            return -1;
        }

        int user_id = budgetDto.getUser_id();
        if (checkingCategoryAndUserIsDuplicate(category_id, user_id) > 0) {
            return -1;
        }

        BudgetEntity budgetEntity = budgetDto.toEntity();
        budgetRepository.save(budgetEntity);
        return budgetEntity.getBudget_id();
    }

    public List<BudgetDto> getBudgets (int user_id) {
        return budgetMapper.getBudgets(user_id);
    }

    @Transactional
    public BudgetDto updateBudget (BudgetDto budgetDto) {
        int category_id = budgetDto.getCategory_id();
        if (!hasBudget(category_id)) {
            return null;
        }

        int user_id = budgetDto.getUser_id();
        if (checkingCategoryAndUserIsDuplicate(category_id, user_id) > 0) {
            return null;
        }

        BudgetEntity budgetEntity = budgetRepository.findById(budgetDto.getBudget_id()).orElse(null);
        if (budgetEntity == null) {
            return null;
        }

        if (user_id != budgetEntity.getUser_id()) {
            return null;
        }

        budgetEntity.setCategory_id(category_id);
        budgetEntity.setAmount(budgetDto.getAmount());

        return budgetEntity.toDto();
    }

    public boolean hasBudget (int category_id) {
        return categoryRepository.existsById(category_id);
    }

    public int checkingCategoryAndUserIsDuplicate (int category_id, int user_id) {
        return budgetMapper.checkCategoryAndUserIsDuplicate(category_id, user_id);
    }
}