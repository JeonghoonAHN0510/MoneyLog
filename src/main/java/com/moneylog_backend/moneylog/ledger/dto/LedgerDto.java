package com.moneylog_backend.moneylog.ledger.dto;

import com.moneylog_backend.global.type.CategoryEnum;
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
    private Integer ledger_id;
    private Integer user_id;
    private Integer category_id;
    private Integer payment_id;
    private Integer account_id;
    private Integer fixed_id;
    private String title;
    private Integer amount;
    private String memo;
    private LocalDate trading_at;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    private CategoryEnum category_type;

    public LedgerEntity toEntity () {
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
    }
}