package com.moneylog_backend.moneylog.transaction.dto.res;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionImportPreviewRowDto {
    private Integer rowIndex;
    private String tradingAt;
    private String title;
    private Integer amount;
    private String memo;
    private Integer installmentCount;
    private Boolean isInterestFree;
    private String accountName;
    private String categoryName;
    private String paymentName;
    private Integer resolvedAccountId;
    private Integer resolvedCategoryId;
    private Integer resolvedPaymentId;
    private List<String> unresolvedFields;
    private List<String> errors;
}
