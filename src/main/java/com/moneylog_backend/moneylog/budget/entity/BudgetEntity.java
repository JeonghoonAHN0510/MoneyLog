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
    @Column(columnDefinition = "INT UNSIGNED")
    private int budget_id;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private int user_id;
    @Column(name = "category_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private int category_id;
    @Column(columnDefinition = "INT NOT NULL")
    private int amount;
    @Column(columnDefinition = "DATE NOT NULL")
    private LocalDate budget_date;

    public BudgetDto toDto () {
        return BudgetDto.builder()
                        .budget_id(this.budget_id)
                        .amount(this.amount)
                        .budget_date(this.budget_date)
                        .user_id(this.user_id)
                        .category_id(this.category_id)
                        .created_at(this.getCreated_at())
                        .updated_at(this.getUpdated_at())
                        .build();
    }
}