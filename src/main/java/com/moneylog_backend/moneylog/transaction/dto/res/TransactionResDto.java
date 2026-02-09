package com.moneylog_backend.moneylog.transaction.dto.res;

import com.moneylog_backend.global.type.CategoryEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class TransactionResDto {
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
}
