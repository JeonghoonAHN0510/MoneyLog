package com.moneylog_backend.moneylog.ledger.dto;

import com.moneylog_backend.moneylog.ledger.entity.LedgerEntity;

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
public class LedgerDto {
	private int ledger_id;
	private int user_id;
	private int category_id;
	private int payment_id;
	private int account_id;
	private int fixed_id;
	private String title;
	private int amount;
	private String memo;
	private LocalDate trading_at;
	private LocalDateTime created_at;
	private LocalDateTime updated_at;

	public LedgerEntity toEntity() {
		return LedgerEntity.builder()
			.ledger_id(this.ledger_id)
			.user_id(this.user_id)
			.category_id(this.category_id)
			.payment_id(payment_id)
			.account_id(this.account_id)
			.fixed_id(fixed_id)
			.title(this.title)
			.amount(this.amount)
			.memo(this.memo)
			.trading_at(this.trading_at)
			.build();
	} // func end
} // class end