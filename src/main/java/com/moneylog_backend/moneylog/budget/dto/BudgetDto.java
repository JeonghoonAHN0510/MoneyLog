package com.moneylog_backend.moneylog.budget.dto;

import com.moneylog_backend.moneylog.budget.entity.BudgetEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BudgetDto {
    private Integer budgetId;
    private Integer userId;
    private Integer categoryId;
    private Integer amount;
    private LocalDate budgetDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String categoryName;

    public BudgetEntity toEntity (Integer userId) {
        return BudgetEntity.builder()
                           .userId(userId)
                           .categoryId(this.categoryId)
                           .amount(this.amount)
                           .budgetDate(LocalDate.now())
                           .build();
    }
}