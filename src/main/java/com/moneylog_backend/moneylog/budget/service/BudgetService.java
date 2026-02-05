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
    public int saveBudget (BudgetDto budgetDto, Integer userId) {
        int categoryId = budgetDto.getCategoryId();
        if (!hasBudget(categoryId)) {
            return -1;
        }

        if (checkingCategoryAndUserIsDuplicate(categoryId, userId) > 0) {
            return -1;
        }

        BudgetEntity budgetEntity = budgetDto.toEntity(userId);
        budgetRepository.save(budgetEntity);
        return budgetEntity.getBudgetId();
    }

    public List<BudgetDto> getBudgets (int userId) {
        return budgetMapper.getBudgets(userId);
    }

    @Transactional
    public BudgetDto updateBudget (BudgetDto budgetDto, Integer userId) {
        int categoryId = budgetDto.getCategoryId();
        if (!hasBudget(categoryId)) {
            return null;
        }

        if (checkingCategoryAndUserIsDuplicate(categoryId, userId) > 0) {
            return null;
        }

        BudgetEntity budgetEntity = getBudgetByIdAndValidateOwnership(budgetDto.getBudgetId(), userId);

        budgetEntity.updateDetails(categoryId, budgetDto.getAmount());

        return budgetEntity.toDto();
    }

    @Transactional
    public boolean deleteBudget (BudgetDto budgetDto) {
        BudgetEntity budgetEntity = getBudgetByIdAndValidateOwnership(budgetDto.getBudgetId(), budgetDto.getUserId());

        budgetRepository.delete(budgetEntity);
        return true;
    }

    private boolean hasBudget (int categoryId) {
        return categoryRepository.existsById(categoryId);
    }

    private int checkingCategoryAndUserIsDuplicate (int categoryId, int userId) {
        return budgetMapper.checkCategoryAndUserIsDuplicate(categoryId, userId);
    }

    private BudgetEntity getBudgetByIdAndValidateOwnership (int budgetId, int userId) {
        BudgetEntity budgetEntity = budgetRepository.findById(budgetId)
                                                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예산입니다."));

        if (userId != budgetEntity.getUserId()) {
            throw new AccessDeniedException("본인의 예산이 아닙니다.");
        }

        return budgetEntity;
    }
}