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
public class TransactionImportUnresolvedIssueDto {
    private Integer rowIndex;
    private String field;
    private String rawValue;
    private String headerColumnLabel;
    private String reasonCode;
    private String valueHint;
}
