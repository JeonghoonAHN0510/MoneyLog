package com.moneylog_backend.moneylog.transaction.importer;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportPreviewRowDto;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportUnresolvedIssueDto;

@Service
public class TransactionDataMapper {
    private static final String UNMAPPED_REFERENCE_LABEL = "<미입력>";
    private static final Map<String, Integer> DEFAULT_COLUMN_ORDER = Map.of(
        "tradingAt", 0,
        "title", 1,
        "amount", 2,
        "accountName", 3,
        "categoryName", 4,
        "paymentName", 5,
        "memo", 6
    );
    private static final List<DateTimeFormatter> SUPPORTED_DATE_FORMATS = List.of(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("yyyy.MM.dd"),
        DateTimeFormatter.ofPattern("yyyyMMdd"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("MM.dd.yyyy")
    );
    private static final String REASON_MISSING = "MISSING";
    private static final String REASON_NOT_FOUND = "NOT_FOUND";
    private static final String REASON_DUPLICATE = "DUPLICATE";
    private static final String REASON_MISALIGNED_LIKELY = "MISALIGNED_LIKELY";
    private static final String HINT_UNKNOWN = "UNKNOWN";
    private static final String HINT_NAME_LIKE = "NAME_LIKE";
    private static final String HINT_MONEY_LIKE = "MONEY_LIKE";

    public boolean isBlankRow (List<String> row) {
        return row.stream().allMatch(String::isBlank);
    }

    public TransactionImportPreviewRowDto buildPreviewRow (int rowIndex,
                                                           List<String> rawRow,
                                                           Map<String, Integer> headerIndex,
                                                           Map<String, List<AccountEntity>> accountLookup,
                                                           Map<String, List<CategoryEntity>> categoryLookup,
                                                           Map<String, List<PaymentEntity>> paymentLookup,
                                                           Set<String> unresolvedAccounts,
                                                           Set<String> unresolvedCategories,
                                                           Set<String> unresolvedPayments,
                                                           Map<String, String> headerLabelByField,
                                                           List<TransactionImportUnresolvedIssueDto> unresolvedIssues) {
        int columnTradingAt = headerIndex.getOrDefault("tradingAt", DEFAULT_COLUMN_ORDER.get("tradingAt"));
        int columnTradingTime = headerIndex.getOrDefault("tradingTime", -1);
        int columnTitle = headerIndex.getOrDefault("title", DEFAULT_COLUMN_ORDER.get("title"));
        int columnAmount = headerIndex.getOrDefault("amount", DEFAULT_COLUMN_ORDER.get("amount"));
        int columnDebitAmount = headerIndex.getOrDefault("debitAmount", -1);
        int columnCreditAmount = headerIndex.getOrDefault("creditAmount", -1);
        int columnAccount = headerIndex.getOrDefault("accountName", DEFAULT_COLUMN_ORDER.get("accountName"));
        int columnCategory = headerIndex.getOrDefault("categoryName", DEFAULT_COLUMN_ORDER.get("categoryName"));
        int columnPayment = headerIndex.getOrDefault("paymentName", DEFAULT_COLUMN_ORDER.get("paymentName"));
        int columnMemo = headerIndex.getOrDefault("memo", DEFAULT_COLUMN_ORDER.get("memo"));

        String tradingAtRaw = getCell(rawRow, columnTradingAt);
        String tradingTimeRaw = getCell(rawRow, columnTradingTime);
        String mergedTradingAtRaw = mergeTradingAtAndTime(tradingAtRaw, tradingTimeRaw);
        String title = getCell(rawRow, columnTitle);
        String amountRaw = getCell(rawRow, columnAmount);
        String debitAmountRaw = getCell(rawRow, columnDebitAmount);
        String creditAmountRaw = getCell(rawRow, columnCreditAmount);
        String accountName = getCell(rawRow, columnAccount);
        String categoryName = getCell(rawRow, columnCategory);
        String paymentName = getCell(rawRow, columnPayment);
        String memo = getCell(rawRow, columnMemo);

        List<String> unresolvedFields = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        String tradingAt = "";
        LocalDate parsedDate = parseDate(mergedTradingAtRaw);
        if (parsedDate == null) {
            errors.add("거래일 형식이 올바르지 않습니다.");
        } else {
            tradingAt = parsedDate.toString();
        }

        Integer amount = parsePositiveAmount(amountRaw);
        String transactionDirection = null;
        if (amount == null) {
            Integer debitAmount = parsePositiveAmount(debitAmountRaw);
            Integer creditAmount = parsePositiveAmount(creditAmountRaw);
            if (debitAmount != null && creditAmount != null) {
                errors.add("출금(원)과 입금(원) 값이 모두 있어 금액을 판별할 수 없습니다.");
                amount = debitAmount;
                transactionDirection = null;
            } else if (debitAmount != null) {
                amount = debitAmount;
                transactionDirection = "DEBIT";
            } else if (creditAmount != null) {
                amount = creditAmount;
                transactionDirection = "CREDIT";
            }
        }
        if (amount == null) {
            errors.add("금액은 숫자(1 이상)로 입력해야 합니다.");
        }

        if (title == null || title.isBlank()) {
            title = "거래";
        }

        Integer resolvedAccountId = null;
        if (accountName == null || accountName.isBlank()) {
            unresolvedFields.add("accountName");
            unresolvedAccounts.add(UNMAPPED_REFERENCE_LABEL);
            addUnresolvedIssue(unresolvedIssues, rowIndex, "accountName", UNMAPPED_REFERENCE_LABEL,
                               resolveHeaderLabel("accountName", columnAccount, headerLabelByField),
                               REASON_MISSING, HINT_UNKNOWN);
        } else {
            List<AccountEntity> candidates = accountLookup.get(normalize(accountName));
            if (candidates == null || candidates.isEmpty()) {
                unresolvedFields.add("accountName");
                unresolvedAccounts.add(accountName);
                addUnresolvedIssue(unresolvedIssues, rowIndex, "accountName", accountName,
                                   resolveHeaderLabel("accountName", columnAccount, headerLabelByField),
                                   classifyUnresolvedReason(accountName),
                                   classifyValueHint(accountName));
            } else if (candidates.size() > 1) {
                unresolvedFields.add("accountName");
                unresolvedAccounts.add(accountName);
                addUnresolvedIssue(unresolvedIssues, rowIndex, "accountName", accountName,
                                   resolveHeaderLabel("accountName", columnAccount, headerLabelByField),
                                   REASON_DUPLICATE, HINT_NAME_LIKE);
                errors.add("동일한 계좌명이 중복되어 계정 매핑이 애매합니다.");
            } else {
                resolvedAccountId = candidates.get(0).getAccountId();
            }
        }

        Integer resolvedCategoryId = null;
        CategoryEnum categoryType = null;
        if (categoryName == null || categoryName.isBlank()) {
            unresolvedFields.add("categoryName");
            unresolvedCategories.add(UNMAPPED_REFERENCE_LABEL);
            errors.add("카테고리명은 필수입니다.");
            addUnresolvedIssue(unresolvedIssues, rowIndex, "categoryName", UNMAPPED_REFERENCE_LABEL,
                               resolveHeaderLabel("categoryName", columnCategory, headerLabelByField),
                               REASON_MISSING, HINT_UNKNOWN);
        } else {
            List<CategoryEntity> candidates = categoryLookup.get(normalize(categoryName));
            if (candidates == null || candidates.isEmpty()) {
                unresolvedFields.add("categoryName");
                unresolvedCategories.add(categoryName);
                addUnresolvedIssue(unresolvedIssues, rowIndex, "categoryName", categoryName,
                                   resolveHeaderLabel("categoryName", columnCategory, headerLabelByField),
                                   classifyUnresolvedReason(categoryName),
                                   classifyValueHint(categoryName));
            } else if (candidates.size() > 1) {
                unresolvedFields.add("categoryName");
                unresolvedCategories.add(categoryName);
                addUnresolvedIssue(unresolvedIssues, rowIndex, "categoryName", categoryName,
                                   resolveHeaderLabel("categoryName", columnCategory, headerLabelByField),
                                   REASON_DUPLICATE, HINT_NAME_LIKE);
                errors.add("동일한 카테고리명이 중복되어 카테고리 매핑이 애매합니다.");
            } else {
                CategoryEntity category = candidates.get(0);
                resolvedCategoryId = category.getCategoryId();
                categoryType = category.getType();
            }
        }

        Integer resolvedPaymentId = null;
        if (CategoryEnum.EXPENSE.equals(categoryType)) {
            if (paymentName == null || paymentName.isBlank()) {
                unresolvedFields.add("paymentName");
                unresolvedPayments.add(paymentName);
                addUnresolvedIssue(unresolvedIssues, rowIndex, "paymentName", UNMAPPED_REFERENCE_LABEL,
                                   resolveHeaderLabel("paymentName", columnPayment, headerLabelByField),
                                   REASON_MISSING, HINT_UNKNOWN);
                errors.add("비용 카테고리는 결제수단이 필요합니다.");
            } else {
                List<PaymentEntity> candidates = paymentLookup.get(normalize(paymentName));
                if (candidates == null || candidates.isEmpty()) {
                    unresolvedFields.add("paymentName");
                    unresolvedPayments.add(paymentName);
                    addUnresolvedIssue(unresolvedIssues, rowIndex, "paymentName", paymentName,
                                       resolveHeaderLabel("paymentName", columnPayment, headerLabelByField),
                                       classifyUnresolvedReason(paymentName),
                                       classifyValueHint(paymentName));
                } else if (candidates.size() > 1) {
                    unresolvedFields.add("paymentName");
                    unresolvedPayments.add(paymentName);
                    addUnresolvedIssue(unresolvedIssues, rowIndex, "paymentName", paymentName,
                                       resolveHeaderLabel("paymentName", columnPayment, headerLabelByField),
                                       REASON_DUPLICATE, HINT_NAME_LIKE);
                    errors.add("동일한 결제수단명이 중복되어 결제수단 매핑이 애매합니다.");
                } else {
                    resolvedPaymentId = candidates.get(0).getPaymentId();
                }
            }
        }

        return TransactionImportPreviewRowDto.builder()
                                            .rowIndex(rowIndex)
                                            .tradingAt(tradingAt)
                                            .title(title)
                                            .transactionDirection(transactionDirection)
                                            .amount(amount)
                                            .memo(memo)
                                            .accountName(accountName)
                                            .categoryName(categoryName)
                                            .paymentName(paymentName)
                                            .resolvedAccountId(resolvedAccountId)
                                            .resolvedCategoryId(resolvedCategoryId)
                                            .resolvedPaymentId(resolvedPaymentId)
                                            .unresolvedFields(unresolvedFields)
                                            .errors(errors)
                                            .build();
    }

    private String classifyUnresolvedReason (String rawValue) {
        if (isMoneyLikeValue(rawValue)) {
            return REASON_MISALIGNED_LIKELY;
        }
        return REASON_NOT_FOUND;
    }

    private String classifyValueHint (String rawValue) {
        if (isMoneyLikeValue(rawValue)) {
            return HINT_MONEY_LIKE;
        }
        return HINT_NAME_LIKE;
    }

    private boolean isMoneyLikeValue (String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        String normalized = raw.replaceAll(",", "").replaceAll("\\s+", "");
        return normalized.matches("^[+-]?\\d{1,15}(\\.\\d+)?$");
    }

    private String resolveHeaderLabel (String field, int fallbackColumnIndex, Map<String, String> headerLabelByField) {
        String headerLabel = headerLabelByField.get(field);
        if (headerLabel == null || headerLabel.isBlank()) {
            return "열#" + (fallbackColumnIndex + 1);
        }
        return headerLabel;
    }

    private void addUnresolvedIssue (List<TransactionImportUnresolvedIssueDto> unresolvedIssues,
                                     Integer rowIndex,
                                     String field,
                                     String rawValue,
                                     String headerColumnLabel,
                                     String reasonCode,
                                     String valueHint) {
        if (rawValue == null) {
            rawValue = "";
        }
        unresolvedIssues.add(TransactionImportUnresolvedIssueDto.builder()
                                                              .rowIndex(rowIndex)
                                                              .field(field)
                                                              .rawValue(rawValue)
                                                              .headerColumnLabel(headerColumnLabel)
                                                              .reasonCode(reasonCode)
                                                              .valueHint(valueHint)
                                                              .build());
    }

    private String normalize (String raw) {
        if (raw == null) {
            return "";
        }
        String value = Normalizer.normalize(raw.trim().toLowerCase(), Normalizer.Form.NFKC);
        return value.replaceAll("\\s+", "");
    }

    private String getCell (List<String> row, int idx) {
        if (idx < 0 || idx >= row.size()) {
            return "";
        }
        return row.get(idx).trim();
    }

    private String mergeTradingAtAndTime (String dateRaw, String timeRaw) {
        if (timeRaw == null || timeRaw.isBlank()) {
            return dateRaw;
        }
        if (dateRaw == null || dateRaw.isBlank()) {
            return timeRaw;
        }
        if (dateRaw.contains(" ")) {
            return dateRaw;
        }
        return dateRaw + " " + timeRaw;
    }

    private LocalDate parseDate (String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        for (DateTimeFormatter formatter : SUPPORTED_DATE_FORMATS) {
            try {
                return LocalDate.parse(raw.trim(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private Integer parsePositiveAmount (String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.replaceAll(",", "").replaceAll("\\s+", "");
        try {
            Integer value = Integer.parseInt(normalized);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
