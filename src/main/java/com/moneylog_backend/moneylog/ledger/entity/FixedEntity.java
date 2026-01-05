package com.moneylog_backend.moneylog.ledger.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.moneylog.ledger.dto.FixedDto;

import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fixed")
@Data
@Builder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class FixedEntity extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INT UNSIGNED")
	private int fixed_id;
	@Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
	private int user_id;
	@Column(name = "category_id", columnDefinition = "INT UNSIGNED NOT NULL")
	private int category_id;
	@Column(columnDefinition = "VARCHAR(100) NOT NULL")
	private String title;
	@Column(columnDefinition = "INT NOT NULL")
	private int amount;
	@Column(columnDefinition = "INT NOT NULL")
	private int fixed_day;
	@Column(columnDefinition = "DATE NOT NULL")
	private LocalDate start_date;
	@Column(columnDefinition = "DATE")
	private LocalDate end_date;

	public FixedDto toDto() {
		return FixedDto.builder()
			.fixed_id(this.fixed_id)
			.user_id(this.user_id)
			.category_id(this.category_id)
			.title(this.title)
			.amount(this.amount)
			.fixed_day(this.fixed_day)
			.start_date(this.start_date)
			.end_date(this.end_date)
			.created_at(this.getCreated_at())
			.updated_at(this.getUpdated_at())
			.build();
	} // func end
} // class end