package com.moneylog_backend.moneylog.budget.dto.res;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class BudgetResDto {
    private Integer budgetId;
    private Integer userId;
    private Integer categoryId;
    private Integer amount;
    private LocalDate budgetDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String categoryName;
}
