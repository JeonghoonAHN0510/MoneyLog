package com.moneylog_backend.moneylog.budget.service;

import java.util.List;

import com.moneylog_backend.global.exception.ResourceNotFoundException;
import com.moneylog_backend.moneylog.budget.dto.req.BudgetReqDto;
import com.moneylog_backend.moneylog.budget.dto.res.BudgetResDto;
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
    public int saveBudget(BudgetReqDto budgetReqDto, Integer userId) {
        int categoryId = budgetReqDto.getCategoryId();
        if (!hasBudget(categoryId)) {
            return -1;
        }

        if (checkingCategoryAndUserIsDuplicate(categoryId, userId) > 0) {
            return -1;
        }

        BudgetEntity budgetEntity = budgetReqDto.toEntity(userId);
        budgetRepository.save(budgetEntity);
        return budgetEntity.getBudgetId();
    }

    public List<BudgetResDto> getBudgets(int userId) {
        return budgetMapper.getBudgets(userId);
    }

    @Transactional
    public BudgetResDto updateBudget(BudgetReqDto budgetReqDto, Integer userId) {
        int categoryId = budgetReqDto.getCategoryId();
        if (!hasBudget(categoryId)) {
            return null;
        }

        if (checkingCategoryAndUserIsDuplicate(categoryId, userId) > 0) {
            return null;
        }

        BudgetEntity budgetEntity = getBudgetByIdAndValidateOwnership(budgetReqDto.getBudgetId(), userId);

        budgetEntity.updateDetails(categoryId, budgetReqDto.getAmount());

        return budgetEntity.toDto();
    }

    @Transactional
    public boolean deleteBudget(int budgetId, int userId) {
        BudgetEntity budgetEntity = getBudgetByIdAndValidateOwnership(budgetId, userId);

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
                                                    .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예산입니다."));

        if (userId != budgetEntity.getUserId()) {
            throw new AccessDeniedException("본인의 예산이 아닙니다.");
        }

        return budgetEntity;
    }
}