package com.moneylog_backend.moneylog.transaction.dto;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.moneylog.transaction.entity.TransactionEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "카테고리 ID는 필수입니다")
    private Integer categoryId;

    private Integer paymentId;
    private Integer accountId;
    private Integer fixedId;

    @Size(max = 100, message = "제목은 100자 이내여야 합니다")
    private String title;

    @NotNull(message = "금액은 필수입니다")
    @Min(value = 1, message = "금액은 1원 이상이어야 합니다")
    private Integer amount;

    @Size(max = 500, message = "메모는 500자 이내여야 합니다")
    private String memo;

    @NotNull(message = "거래일은 필수입니다")
    private LocalDate tradingAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CategoryEnum categoryType;
    private String categoryName;
    private String paymentName;
    private Integer installmentPlanId;
    private Integer installmentNo;
    private Integer installmentTotalCount;
    private Boolean isInstallment;
    private Boolean isInterestFree;
    private Boolean isSettled;
    private LocalDateTime settledAt;

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
                                .installmentPlanId(this.installmentPlanId)
                                .installmentNo(this.installmentNo)
                                .installmentTotalCount(this.installmentTotalCount)
                                .isInstallment(this.isInstallment)
                                .isInterestFree(this.isInterestFree)
                                .isSettled(this.isSettled)
                                .settledAt(this.settledAt)
                                .tradingAt(this.tradingAt)
                                .build();
    }
}
