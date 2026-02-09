package com.moneylog_backend.moneylog.budget.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.moneylog.budget.dto.res.BudgetResDto;

import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "budget")
@Getter
@SuperBuilder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BudgetEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id", columnDefinition = "INT UNSIGNED")
    private Integer budgetId;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer userId;
    @Column(name = "category_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer categoryId;
    @Column(columnDefinition = "INT NOT NULL")
    private Integer amount;
    @Column(name = "budget_date", columnDefinition = "DATE NOT NULL")
    private LocalDate budgetDate;

    public BudgetResDto toDto() {
        return BudgetResDto.builder()
                        .budgetId(this.budgetId)
                        .amount(this.amount)
                        .budgetDate(this.budgetDate)
                        .userId(this.userId)
                        .categoryId(this.categoryId)
                        .createdAt(this.getCreatedAt())
                        .updatedAt(this.getUpdatedAt())
                        .build();
    }

    public void updateDetails(Integer categoryId, Integer amount) {
        if (categoryId != null) {
            this.categoryId = categoryId;
        }

        if (amount != null) {
            this.amount = amount;
        }
    }
}