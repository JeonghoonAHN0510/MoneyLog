package com.moneylog_backend.moneylog.budget.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.moneylog.budget.dto.BudgetDto;

import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "budget")
@Data
@Builder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class BudgetEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id", columnDefinition = "INT UNSIGNED")
    private int budgetId;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private int userId;
    @Column(name = "category_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private int categoryId;
    @Column(columnDefinition = "INT NOT NULL")
    private int amount;
    @Column(name = "budget_date", columnDefinition = "DATE NOT NULL")
    private LocalDate budgetDate;

    public BudgetDto toDto () {
        return BudgetDto.builder()
                        .budgetId(this.budgetId)
                        .amount(this.amount)
                        .budgetDate(this.budgetDate)
                        .userId(this.userId)
                        .categoryId(this.categoryId)
                        .createdAt(this.getCreatedAt())
                        .updatedAt(this.getUpdatedAt())
                        .build();
    }
}