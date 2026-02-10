package com.moneylog_backend.moneylog.budget.dto.req;

import com.moneylog_backend.moneylog.budget.entity.BudgetEntity;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BudgetReqDto {
    private Integer budgetId;

    @NotNull(message = "카테고리 ID는 필수입니다")
    private Integer categoryId;

    @NotNull(message = "예산 금액은 필수입니다")
    @Min(value = 1, message = "예산은 1원 이상이어야 합니다")
    private Integer amount;

    public BudgetEntity toEntity(Integer userId) {
        return BudgetEntity.builder()
                           .userId(userId)
                           .categoryId(this.categoryId)
                           .amount(this.amount)
                           .budgetDate(LocalDate.now())
                           .build();
    }
}
