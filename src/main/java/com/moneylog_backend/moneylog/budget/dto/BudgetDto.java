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
	private int budget_id;
	private int user_id;
	private int category_id;
	private int amount;
	private LocalDate budget_date;
	private LocalDateTime created_at;
	private LocalDateTime updated_at;

	public BudgetEntity budgetEntity() {
		return BudgetEntity.builder()
			.budget_id(this.budget_id)
			.user_id(this.user_id)
			.category_id(this.category_id)
			.amount(this.amount)
			.budget_date(this.budget_date)
			.build();
	} // func end
} // class end