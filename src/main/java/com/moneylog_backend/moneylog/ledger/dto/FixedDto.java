package com.moneylog_backend.moneylog.ledger.dto;

import com.moneylog_backend.moneylog.ledger.entity.FixedEntity;

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
public class FixedDto {
    private Integer fixed_id;
    private Integer user_id;
    private Integer category_id;
    private String title;
    private Integer amount;
    private Integer fixed_day;
    private LocalDate start_date;
    private LocalDate end_date;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public FixedEntity toEntity () {
        return FixedEntity.builder()
                          .fixed_id(this.fixed_id)
                          .user_id(this.user_id)
                          .category_id(this.category_id)
                          .title(this.title)
                          .amount(this.amount)
                          .fixed_day(this.fixed_day)
                          .start_date(this.start_date)
                          .end_date(this.end_date)
                          .build();
    }
}