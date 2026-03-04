package com.moneylog_backend.moneylog.transaction.dto.req;

import java.time.LocalDate;

import com.moneylog_backend.moneylog.transaction.dto.req.TransactionReqDto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionImportCommitRowDto {
    private Integer rowIndex;
    private LocalDate tradingAt;
    private String title;
    private Integer amount;
    private String memo;
    private Integer installmentCount;
    private Boolean isInterestFree;
    private Integer accountId;
    private Integer categoryId;
    private Integer paymentId;

    public TransactionReqDto toTransactionReqDto () {
        return TransactionReqDto.builder()
                               .tradingAt(this.tradingAt)
                               .title(this.title)
                               .amount(this.amount)
                               .memo(this.memo)
                               .installmentCount(this.installmentCount)
                               .isInterestFree(this.isInterestFree)
                               .accountId(this.accountId)
                               .categoryId(this.categoryId)
                               .paymentId(this.paymentId)
                               .build();
    }
}
