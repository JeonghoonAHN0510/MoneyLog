package com.moneylog_backend.moneylog.ledger.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.moneylog.ledger.dto.TransferDto;

import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transfer")
@Data
@Builder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class TransferEntity extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private int transfer_id;
	@Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
	private int user_id;
	@Column(name = "from_account", columnDefinition = "INT UNSIGNED NOT NULL")
	private int from_account;
	@Column(name = "to_account", columnDefinition = "INT UNSIGNED NOT NULL")
	private int to_account;
	@Column(columnDefinition = "INT NOT NULL")
	private int amount;
	@Column(columnDefinition = "DATE NOT NULL")
	private LocalDate transfer_at;
	@Column(columnDefinition = "TEXT")
	private String memo;

	public TransferDto toDto() {
		return TransferDto.builder()
			.transfer_id(this.transfer_id)
			.user_id(this.user_id)
			.from_account(this.from_account)
			.to_account(this.to_account)
			.amount(this.amount)
			.transfer_at(this.transfer_at)
			.memo(this.memo)
			.created_at(this.getCreated_at())
			.updated_at(this.getUpdated_at())
			.build();
	} // func end
} // class end