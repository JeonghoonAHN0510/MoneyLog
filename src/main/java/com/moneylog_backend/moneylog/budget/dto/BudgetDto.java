package com.moneylog_backend.moneylog.budget.dto;

import com.moneylog_backend.moneylog.budget.entity.BudgetEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetDto {
    private int budgetId;
    private int userId;
    private int categoryId;
    private int amount;
    private LocalDate budgetDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String categoryName;

    public BudgetEntity toEntity () {
        return BudgetEntity.builder()
                           .budgetId(this.budgetId)
                           .userId(this.userId)
                           .categoryId(this.categoryId)
                           .amount(this.amount)
                           .budgetDate(LocalDate.now())
                           .build();
    }
}