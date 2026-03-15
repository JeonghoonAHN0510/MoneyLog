package com.moneylog_backend.moneylog.transaction.dto.req;

import com.moneylog_backend.moneylog.transaction.entity.TransactionEntity;

import java.time.LocalDate;

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
public class TransactionReqDto {
    private Integer transactionId;

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

    @Min(value = 2, message = "할부 개월 수는 2 이상이어야 합니다")
    private Integer installmentCount;
    private Boolean isInterestFree;

    @NotNull(message = "거래일은 필수입니다")
    private LocalDate tradingAt;

    public boolean isInstallment () {
        return this.installmentCount != null && this.installmentCount >= 2;
    }

    public TransactionEntity toEntity(Integer userId, String normalizedTitle, String normalizedMemo) {
        return TransactionEntity.builder()
                                .userId(userId)
                                .categoryId(this.categoryId)
                                .paymentId(this.paymentId)
                                .accountId(this.accountId)
                                .fixedId(fixedId)
                                .title(normalizedTitle)
                                .amount(this.amount)
                                .memo(normalizedMemo)
                                .isInterestFree(this.isInterestFree)
                                .tradingAt(this.tradingAt)
                                .build();
    }
}
