package com.moneylog_backend.moneylog.budget.dto;

import com.moneylog_backend.moneylog.budget.entity.BudgetEntity;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.user.entity.UserEntity;

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
    private int budget_id;
    private int user_id;
    private int category_id;
    private int amount;
    private LocalDate budget_date;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public BudgetEntity budgetEntity(UserEntity userEntity,
                                     CategoryEntity categoryEntity) {
        return BudgetEntity.builder()
                .budget_id(this.budget_id)
                .userEntity(userEntity)
                .categoryEntity(categoryEntity)
                .amount(this.amount)
                .budget_date(this.budget_date)
                .build();
    } // func end
} // class end