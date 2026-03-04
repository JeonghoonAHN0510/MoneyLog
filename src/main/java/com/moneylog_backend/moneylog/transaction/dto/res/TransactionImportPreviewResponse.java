package com.moneylog_backend.moneylog.transaction.dto.res;

import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionImportPreviewResponse {
    private List<TransactionImportPreviewRowDto> rows;
    private TransactionImportSummaryDto summary;
    private Set<String> unresolvedAccounts;
    private Set<String> unresolvedCategories;
    private Set<String> unresolvedPayments;
    private List<TransactionImportUnresolvedIssueDto> unresolvedIssues;
    private List<TransactionImportReferenceDto> availableAccounts;
    private List<TransactionImportReferenceDto> availableCategories;
    private List<TransactionImportReferenceDto> availablePayments;
}
