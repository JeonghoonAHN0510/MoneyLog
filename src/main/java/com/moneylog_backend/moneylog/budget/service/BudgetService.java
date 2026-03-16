package com.moneylog_backend.moneylog.budget.service;

import java.util.List;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.exception.ResourceNotFoundException;
import com.moneylog_backend.global.util.OwnershipValidator;
import com.moneylog_backend.moneylog.budget.dto.req.BudgetReqDto;
import com.moneylog_backend.moneylog.budget.dto.res.BudgetResDto;
import com.moneylog_backend.moneylog.budget.entity.BudgetEntity;
import com.moneylog_backend.moneylog.budget.mapper.BudgetMapper;
import com.moneylog_backend.moneylog.budget.repository.BudgetRepository;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;

import org.springframework.dao.DataIntegrityViolationException;
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
        validateCategoryOwnership(categoryId, userId);

        if (checkingCategoryAndUserIsDuplicate(categoryId, userId) > 0) {
            return -1;
        }

        BudgetEntity budgetEntity = budgetReqDto.toEntity(userId);
        try {
            budgetRepository.saveAndFlush(budgetEntity);
        } catch (DataIntegrityViolationException ex) {
            return -1;
        }
        return budgetEntity.getBudgetId();
    }

    public List<BudgetResDto> getBudgets(int userId) {
        return budgetMapper.getBudgets(userId);
    }

    @Transactional
    public BudgetResDto updateBudget(BudgetReqDto budgetReqDto, Integer userId) {
        int categoryId = budgetReqDto.getCategoryId();
        validateCategoryOwnership(categoryId, userId);

        BudgetEntity budgetEntity = getBudgetByIdAndValidateOwnership(budgetReqDto.getBudgetId(), userId);

        budgetEntity.updateDetails(categoryId, budgetReqDto.getAmount());
        try {
            budgetRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("이미 등록된 예산입니다.");
        }

        return budgetEntity.toDto();
    }

    @Transactional
    public boolean deleteBudget(int budgetId, int userId) {
        BudgetEntity budgetEntity = getBudgetByIdAndValidateOwnership(budgetId, userId);

        budgetRepository.delete(budgetEntity);
        return true;
    }

    private int checkingCategoryAndUserIsDuplicate (int categoryId, int userId) {
        return budgetMapper.checkCategoryAndUserIsDuplicate(categoryId, userId);
    }

    private void validateCategoryOwnership (int categoryId, int userId) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
                                                          .orElseThrow(() -> new ResourceNotFoundException(
                                                              ErrorMessageConstants.CATEGORY_NOT_FOUND));

        OwnershipValidator.validateOwner(categoryEntity.getUserId(), userId, "본인의 카테고리가 아닙니다.");
    }

    private BudgetEntity getBudgetByIdAndValidateOwnership (int budgetId, int userId) {
        BudgetEntity budgetEntity = budgetRepository.findById(budgetId)
                                                    .orElseThrow(() -> new ResourceNotFoundException(
                                                        ErrorMessageConstants.BUDGET_NOT_FOUND));

        OwnershipValidator.validateOwner(budgetEntity.getUserId(), userId, "본인의 예산이 아닙니다.");

        return budgetEntity;
    }
}
