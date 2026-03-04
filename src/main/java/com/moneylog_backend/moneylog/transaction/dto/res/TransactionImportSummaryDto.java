package com.moneylog_backend.moneylog.transaction.dto.res;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionImportSummaryDto {
    private Integer totalRows;
    private Integer resolvedRows;
    private Integer unresolvedRows;
    private Integer invalidRows;
}
