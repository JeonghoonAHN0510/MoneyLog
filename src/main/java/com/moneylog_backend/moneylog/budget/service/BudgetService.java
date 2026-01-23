package com.moneylog_backend.moneylog.budget.service;

import java.util.List;

import com.moneylog_backend.moneylog.budget.dto.BudgetDto;
import com.moneylog_backend.moneylog.budget.entity.BudgetEntity;
import com.moneylog_backend.moneylog.budget.mapper.BudgetMapper;
import com.moneylog_backend.moneylog.budget.repository.BudgetRepository;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;

import org.springframework.security.access.AccessDeniedException;
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

        BudgetEntity budgetEntity = getBudgetEntityById(budgetDto.getBudget_id(), user_id);

        budgetEntity.setCategory_id(category_id);
        budgetEntity.setAmount(budgetDto.getAmount());

        return budgetEntity.toDto();
    }

    @Transactional
    public boolean deleteBudget (BudgetDto budgetDto) {
        BudgetEntity budgetEntity = getBudgetEntityById(budgetDto.getBudget_id(), budgetDto.getUser_id());

        budgetRepository.delete(budgetEntity);
        return true;
    }

    private boolean hasBudget (int category_id) {
        return categoryRepository.existsById(category_id);
    }

    private int checkingCategoryAndUserIsDuplicate (int category_id, int user_id) {
        return budgetMapper.checkCategoryAndUserIsDuplicate(category_id, user_id);
    }

    private BudgetEntity getBudgetEntityById (int budget_id, int user_id) {
        BudgetEntity budgetEntity = budgetRepository.findById(budget_id)
                                                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예산입니다."));

        if (user_id != budgetEntity.getUser_id()) {
            throw new AccessDeniedException("본인의 예산이 아닙니다.");
        }

        return budgetEntity;
    }
}