package com.moneylog_backend.moneylog.budget.service;

import java.time.LocalDate;

import com.moneylog_backend.moneylog.budget.dto.BudgetDto;
import com.moneylog_backend.moneylog.budget.entity.BudgetEntity;
import com.moneylog_backend.moneylog.budget.mapper.BudgetMapper;
import com.moneylog_backend.moneylog.budget.repository.BudgetRepository;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;

    public int saveBudget (BudgetDto budgetDto) {
        int category_id = budgetDto.getCategory_id();
        if (!checkingCategoryIsValid(category_id)) {
            return -1;
        }

        int user_id = budgetDto.getUser_id();
        if (checkingCategoryAndUserIsDuplicate(category_id, user_id) > 0) {
            return -1;
        }

        LocalDate today = LocalDate.now();
        budgetDto.setBudget_date(today);

        BudgetEntity budgetEntity = budgetDto.toEntity();
        budgetRepository.save(budgetEntity);
        return budgetEntity.getBudget_id();
    }

    public boolean checkingCategoryIsValid (int category_id) {
        return categoryRepository.existsById(category_id);
    }

    public int checkingCategoryAndUserIsDuplicate (int category_id, int user_id) {
        return budgetMapper.checkCategoryAndUserIsDuplicate(category_id, user_id);
    }
}