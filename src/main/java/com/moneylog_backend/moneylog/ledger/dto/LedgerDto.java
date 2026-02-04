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
    private Integer ledgerId;
    private Integer userId;
    private Integer categoryId;
    private Integer paymentId;
    private Integer accountId;
    private Integer fixedId;
    private String title;
    private Integer amount;
    private String memo;
    private LocalDate tradingAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CategoryEnum categoryType;

    public LedgerEntity toEntity () {
        return LedgerEntity.builder()
                           .userId(this.userId)
                           .categoryId(this.categoryId)
                           .paymentId(this.paymentId)
                           .accountId(this.accountId)
                           .fixedId(fixedId)
                           .title(this.title)
                           .amount(this.amount)
                           .memo(this.memo)
                           .tradingAt(this.tradingAt)
                           .build();
    }
}