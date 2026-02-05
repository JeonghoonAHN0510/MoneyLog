package com.moneylog_backend.moneylog.transaction.dto;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.moneylog.transaction.entity.TransactionEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionDto {
    private Integer transactionId;
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
    private String categoryName;
    private String paymentName;

    public TransactionEntity toEntity (Integer userId) {
        return TransactionEntity.builder()
                                .userId(userId)
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