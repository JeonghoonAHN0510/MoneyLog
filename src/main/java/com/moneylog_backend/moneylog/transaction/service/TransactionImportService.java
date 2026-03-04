package com.moneylog_backend.moneylog.transaction.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Objects;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;
import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;
import com.moneylog_backend.moneylog.payment.repository.PaymentRepository;
import com.moneylog_backend.moneylog.transaction.dto.req.TransactionImportCommitRequest;
import com.moneylog_backend.moneylog.transaction.dto.req.TransactionImportCommitRowDto;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportCommitResponse;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportPreviewResponse;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportPreviewRowDto;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportReferenceDto;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportUnresolvedIssueDto;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportSummaryDto;
import com.moneylog_backend.moneylog.transaction.dto.req.TransactionReqDto;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionImportService {
    private static final String UNMAPPED_REFERENCE_LABEL = "<미입력>";

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
    private static final String HEADER_PROFILE_RESOURCE = "/transaction-import-header-profile.json";
    private static final HeaderImportProfile HEADER_PROFILE = loadHeaderProfile();
    private static final Map<String, Integer> DEFAULT_COLUMN_ORDER = Map.of(
        "tradingAt", 0,
        "title", 1,
        "amount", 2,
        "accountName", 3,
        "categoryName", 4,
        "paymentName", 5,
        "memo", 6,
        "installmentCount", 7,
        "isInterestFree", 8
    );
    private static final Set<String> TRUE_TEXT = Set.of("y", "yes", "true", "1", "o", "예", "네");
    private static final Set<String> FALSE_TEXT = Set.of("n", "no", "false", "0", "아니오", "아니요", "미적용");
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();
    private static final String REASON_MISSING = "MISSING";
    private static final String REASON_NOT_FOUND = "NOT_FOUND";
    private static final String REASON_DUPLICATE = "DUPLICATE";
    private static final String REASON_MISALIGNED_LIKELY = "MISALIGNED_LIKELY";
    private static final String HINT_UNKNOWN = "UNKNOWN";
    private static final String HINT_NAME_LIKE = "NAME_LIKE";
    private static final String HINT_MONEY_LIKE = "MONEY_LIKE";

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionService transactionService;

    @Transactional(readOnly = true)
    public TransactionImportPreviewResponse previewImport (MultipartFile file, Integer userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String fileName = safeFileName(file.getOriginalFilename()).toLowerCase();
        List<List<String>> rawRows = parseRowsByFile(fileName, file);
        if (rawRows.isEmpty()) {
            throw new IllegalArgumentException("임포트 대상 데이터가 없습니다.");
        }

        List<AccountEntity> accounts = accountRepository.findByUserId(userId);
        List<CategoryEntity> categories = categoryRepository.findByUserId(userId);
        List<PaymentEntity> payments = paymentRepository.findByUserId(userId);

        Map<String, List<AccountEntity>> accountLookup = buildLookupByName(accounts);
        Map<String, List<CategoryEntity>> categoryLookup = buildLookupByName(categories);
        Map<String, List<PaymentEntity>> paymentLookup = buildLookupByName(payments);

        int headerRowIndex = resolveHeaderRowIndex(rawRows);
        Map<String, Integer> headerIndex = headerRowIndex >= 0 ? resolveHeaderIndex(rawRows.get(headerRowIndex)) : Map.of();
        Map<String, String> headerLabelByField = headerRowIndex >= 0
            ? resolveHeaderLabelByField(rawRows.get(headerRowIndex), headerIndex)
            : Map.of();
        int startRow = headerRowIndex >= 0 ? headerRowIndex + 1 : 0;

        int totalRows = 0;
        int resolvedRows = 0;
        int unresolvedRows = 0;
        int invalidRows = 0;
        Set<String> unresolvedAccounts = new TreeSet<>();
        Set<String> unresolvedCategories = new TreeSet<>();
        Set<String> unresolvedPayments = new TreeSet<>();
        List<TransactionImportUnresolvedIssueDto> unresolvedIssues = new ArrayList<>();
        List<TransactionImportPreviewRowDto> rows = new ArrayList<>();

        for (int i = startRow; i < rawRows.size(); i++) {
            List<String> rawRow = rawRows.get(i);
            if (isBlankRow(rawRow)) {
                continue;
            }

            TransactionImportPreviewRowDto previewRow = buildPreviewRow(
                i - startRow + 1,
                rawRow,
                headerIndex,
                accountLookup,
                categoryLookup,
                paymentLookup,
                unresolvedAccounts,
                unresolvedCategories,
                unresolvedPayments,
                headerLabelByField,
                unresolvedIssues
            );

            totalRows++;
            if (!previewRow.getErrors().isEmpty()) {
                invalidRows++;
            } else if (!previewRow.getUnresolvedFields().isEmpty()) {
                unresolvedRows++;
            } else {
                resolvedRows++;
            }
            rows.add(previewRow);
        }

        return TransactionImportPreviewResponse.builder()
                                              .rows(rows)
                                              .summary(TransactionImportSummaryDto.builder()
                                                                                .totalRows(totalRows)
                                                                                .resolvedRows(resolvedRows)
                                                                                .unresolvedRows(unresolvedRows)
                                                                                .invalidRows(invalidRows)
                                                                                .build())
                                              .unresolvedAccounts(unresolvedAccounts)
                                              .unresolvedCategories(unresolvedCategories)
                                              .unresolvedPayments(unresolvedPayments)
                                              .unresolvedIssues(unresolvedIssues)
                                              .availableAccounts(toAccountReferenceList(accounts))
                                              .availableCategories(toCategoryReferenceList(categories))
                                              .availablePayments(toPaymentReferenceList(payments))
                                              .build();
    }

    @Transactional
    public TransactionImportCommitResponse commitImport (TransactionImportCommitRequest request, Integer userId) {
        if (request == null || request.getRows() == null || request.getRows().isEmpty()) {
            throw new IllegalArgumentException("저장할 항목이 없습니다.");
        }

        List<Integer> createdTransactionIds = new ArrayList<>();
        for (TransactionImportCommitRowDto row : request.getRows()) {
            validateCommitRow(row, userId);
            TransactionReqDto txReq = row.toTransactionReqDto();
            createdTransactionIds.add(transactionService.saveTransaction(txReq, userId));
        }

        return TransactionImportCommitResponse.builder()
                                             .requestedCount(request.getRows().size())
                                             .importedCount(createdTransactionIds.size())
                                             .transactionIds(createdTransactionIds)
                                             .build();
    }

    private List<List<String>> parseRowsByFile (String fileName, MultipartFile file) {
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            return parseExcel(file);
        }
        if (fileName.endsWith(".csv")) {
            return parseCsv(file);
        }
        throw new IllegalArgumentException("CSV 또는 Excel(xlsx/xls) 파일만 업로드할 수 있습니다.");
    }

    private List<List<String>> parseCsv (MultipartFile file) {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (rows.isEmpty() && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                rows.add(parseCsvLine(line));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("CSV 파일을 읽는 중 오류가 발생했습니다.");
        }
        return rows;
    }

    private List<String> parseCsvLine (String line) {
        List<String> values = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                    continue;
                }
                inQuotes = !inQuotes;
                continue;
            }
            if (ch == ',' && !inQuotes) {
                values.add(sb.toString().trim());
                sb.setLength(0);
                continue;
            }
            sb.append(ch);
        }
        values.add(sb.toString().trim());
        return values;
    }

    private List<List<String>> parseExcel (MultipartFile file) {
        List<List<String>> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            var sheet = workbook.getSheetAt(0);
            for (int rowIdx = 0; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) {
                    rows.add(List.of());
                    continue;
                }
                int lastCell = Math.max(0, row.getLastCellNum());
                List<String> values = new ArrayList<>();
                for (int col = 0; col <= lastCell; col++) {
                    values.add(cellToString(row.getCell(col)));
                }
                rows.add(values);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Excel 파일을 읽는 중 오류가 발생했습니다.");
        }
        return rows;
    }

    private String cellToString (Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate().toString();
        }
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }

    private Map<String, Integer> resolveHeaderIndex (List<String> headerRow) {
        Map<String, Integer> found = new HashMap<>();
        for (int i = 0; i < headerRow.size(); i++) {
            String token = normalize(headerRow.get(i));
            if (token.isBlank()) {
                continue;
            }
            String field = detectHeaderField(token);
            if (field != null) {
                found.put(field, i);
            }
        }
        return found;
    }

    private int resolveHeaderRowIndex (List<List<String>> rawRows) {
        int bestRow = -1;
        int bestScore = 0;

        int maxScan = Math.min(rawRows.size(), HEADER_PROFILE.headerScanLimit());
        for (int i = 0; i < maxScan; i++) {
            List<String> row = rawRows.get(i);
            int score = headerRowScore(row);
            if (score >= HEADER_PROFILE.headerMatchThreshold() && score > bestScore) {
                bestScore = score;
                bestRow = i;
            }
        }

        return bestRow;
    }

    private int headerRowScore (List<String> row) {
        Set<String> matchedFields = new HashSet<>();
        for (String rawCell : row) {
            String token = normalize(rawCell);
            if (token.isBlank()) {
                continue;
            }
            String field = detectHeaderField(token);
            if (field != null) {
                matchedFields.add(field);
            }
        }
        return matchedFields.size();
    }

    private TransactionImportPreviewRowDto buildPreviewRow (int rowIndex,
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
        int columnIsInterestFree = headerIndex.getOrDefault("isInterestFree", DEFAULT_COLUMN_ORDER.get("isInterestFree"));

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
        String isInterestFreeRaw = getCell(rawRow, columnIsInterestFree);

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
        Integer installmentCount = null;

        Boolean isInterestFree = parseBoolean(isInterestFreeRaw);

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
                                            .installmentCount(installmentCount)
                                            .isInterestFree(isInterestFree)
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

    private Map<String, String> resolveHeaderLabelByField (List<String> headerRow, Map<String, Integer> headerIndex) {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, Integer> entry : headerIndex.entrySet()) {
            int columnIndex = entry.getValue();
            if (columnIndex >= 0 && columnIndex < headerRow.size()) {
                headers.put(entry.getKey(), headerRow.get(columnIndex).trim());
            }
        }
        return headers;
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

    private void validateCommitRow (TransactionImportCommitRowDto row, Integer userId) {
        if (row.getTradingAt() == null) {
            throw new IllegalArgumentException("거래일은 필수입니다.");
        }
        if (row.getAccountId() == null) {
            throw new IllegalArgumentException("계좌 ID는 필수입니다.");
        }
        if (row.getCategoryId() == null) {
            throw new IllegalArgumentException("카테고리 ID는 필수입니다.");
        }
        CategoryEntity categoryEntity = categoryRepository.findById(row.getCategoryId())
                                                       .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        if (!Objects.equals(categoryEntity.getUserId(), userId)) {
            throw new IllegalArgumentException("권한이 없는 카테고리입니다.");
        }
        if (CategoryEnum.EXPENSE.equals(categoryEntity.getType()) && row.getPaymentId() == null) {
            throw new IllegalArgumentException("비용 카테고리는 결제수단이 필요합니다.");
        }
        if (row.getAmount() == null || row.getAmount() <= 0) {
            throw new IllegalArgumentException("금액은 1 이상이어야 합니다.");
        }
        if (row.getInstallmentCount() != null && row.getInstallmentCount() > 0 && row.getInstallmentCount() < 2) {
            throw new IllegalArgumentException("할부 개월 수는 2 이상이어야 합니다.");
        }
    }

    private <T> Map<String, List<T>> buildLookupByName (List<T> entities) {
        Map<String, List<T>> lookup = new HashMap<>();
        for (T entity : entities) {
            String name = extractName(entity);
            if (name == null || name.isBlank()) {
                continue;
            }
            String key = normalize(name);
            lookup.computeIfAbsent(key, ignored -> new ArrayList<>()).add(entity);
        }
        return lookup;
    }

    private <T> String extractName (T entity) {
        if (entity instanceof AccountEntity account) {
            return account.getNickname();
        }
        if (entity instanceof CategoryEntity category) {
            return category.getName();
        }
        if (entity instanceof PaymentEntity payment) {
            return payment.getName();
        }
        return "";
    }

    private List<TransactionImportReferenceDto> toAccountReferenceList (List<AccountEntity> accounts) {
        List<TransactionImportReferenceDto> references = new ArrayList<>();
        for (AccountEntity account : accounts) {
            references.add(TransactionImportReferenceDto.builder()
                                                     .id(account.getAccountId())
                                                     .name(account.getNickname())
                                                     .type("ACCOUNT")
                                                     .build());
        }
        references.sort(Comparator.comparing(TransactionImportReferenceDto::getName, String.CASE_INSENSITIVE_ORDER));
        return references;
    }

    private List<TransactionImportReferenceDto> toCategoryReferenceList (List<CategoryEntity> categories) {
        List<TransactionImportReferenceDto> references = new ArrayList<>();
        for (CategoryEntity category : categories) {
            references.add(TransactionImportReferenceDto.builder()
                                                     .id(category.getCategoryId())
                                                     .name(category.getName())
                                                     .type(category.getType().name())
                                                     .build());
        }
        references.sort(Comparator.comparing(TransactionImportReferenceDto::getName, String.CASE_INSENSITIVE_ORDER));
        return references;
    }

    private List<TransactionImportReferenceDto> toPaymentReferenceList (List<PaymentEntity> payments) {
        List<TransactionImportReferenceDto> references = new ArrayList<>();
        for (PaymentEntity payment : payments) {
            references.add(TransactionImportReferenceDto.builder()
                                                     .id(payment.getPaymentId())
                                                     .name(payment.getName())
                                                     .type(payment.getType().name())
                                                     .build());
        }
        references.sort(Comparator.comparing(TransactionImportReferenceDto::getName, String.CASE_INSENSITIVE_ORDER));
        return references;
    }

    private String normalize (String raw) {
        if (raw == null) {
            return "";
        }
        String value = Normalizer.normalize(raw.trim().toLowerCase(), Normalizer.Form.NFKC);
        return value.replaceAll("\\s+", "");
    }

    private String detectHeaderField (String normalizedToken) {
        for (String field : HEADER_PROFILE.fieldMatchOrder()) {
            Set<String> aliases = HEADER_PROFILE.aliasesFor(field);
            if (matchesAnyAlias(normalizedToken, aliases)) {
                return field;
            }
        }
        return null;
    }

    private boolean matchesAnyAlias (String normalizedToken, Set<String> candidateTokens) {
        return candidateTokens.stream().anyMatch(token -> normalizedToken.contains(normalize(token)));
    }

    private boolean isBlankRow (List<String> row) {
        return row.stream().allMatch(String::isBlank);
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

    private Boolean parseBoolean (String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        String normalized = raw.trim().toLowerCase();
        if (TRUE_TEXT.contains(normalized)) {
            return true;
        }
        if (FALSE_TEXT.contains(normalized)) {
            return false;
        }
        return false;
    }

    private String safeFileName (String fileName) {
        return fileName == null ? "" : fileName.trim().toLowerCase();
    }

    private static HeaderImportProfile loadHeaderProfile () {
        try (InputStream inputStream = TransactionImportService.class.getResourceAsStream(HEADER_PROFILE_RESOURCE)) {
            if (inputStream == null) {
                return HeaderImportProfile.createDefault();
            }

            JsonNode root = new ObjectMapper().readTree(inputStream);
            int headerScanLimit = root.path("headerScanLimit").asInt(40);
            int headerMatchThreshold = root.path("headerMatchThreshold").asInt(2);

            Map<String, Set<String>> aliases = new HashMap<>();
            JsonNode aliasesNode = root.path("aliases");
            if (aliasesNode.isObject()) {
                aliasesNode.fieldNames()
                           .forEachRemaining(field -> aliases.put(field, parseAliasSet(aliasesNode.path(field))));
            }

            List<String> fieldMatchOrder = new ArrayList<>();
            JsonNode orderNode = root.path("fieldMatchOrder");
            if (orderNode.isArray()) {
                for (JsonNode token : orderNode) {
                    if (token.isTextual()) {
                        fieldMatchOrder.add(token.asText());
                    }
                }
            }
            if (fieldMatchOrder.isEmpty()) {
                fieldMatchOrder = List.of("tradingAt", "tradingTime", "title", "amount", "debitAmount", "creditAmount", "accountName",
                                         "categoryName", "paymentName", "memo", "installmentCount", "isInterestFree");
            }

            return new HeaderImportProfile(
                headerScanLimit,
                headerMatchThreshold,
                aliases,
                fieldMatchOrder
            );
        } catch (Exception e) {
            return HeaderImportProfile.createDefault();
        }
    }

    private static Set<String> parseAliasSet (JsonNode node) {
        Set<String> aliasSet = new HashSet<>();
        if (!node.isArray()) {
            return Set.of();
        }
        for (JsonNode element : node) {
            if (element.isTextual()) {
                String value = element.asText();
                if (!value.isBlank()) {
                    aliasSet.add(value);
                }
            }
        }
        return aliasSet;
    }

    private static class HeaderImportProfile {
        private final int headerScanLimit;
        private final int headerMatchThreshold;
        private final Map<String, Set<String>> aliases;
        private final List<String> fieldMatchOrder;

        private HeaderImportProfile (int headerScanLimit,
                                    int headerMatchThreshold,
                                    Map<String, Set<String>> aliases,
                                    List<String> fieldMatchOrder) {
            this.headerScanLimit = headerScanLimit;
            this.headerMatchThreshold = headerMatchThreshold;
            this.aliases = aliases;
            this.fieldMatchOrder = fieldMatchOrder;
        }

        private static HeaderImportProfile createDefault () {
            Map<String, Set<String>> aliases = new HashMap<>();
            aliases.put("tradingAt", Set.of("거래일", "거래일자", "일자", "날짜", "date"));
            aliases.put("tradingTime", Set.of("거래시간", "시간"));
            aliases.put("title", Set.of("title", "제목", "내용", "적요"));
            aliases.put("amount", Set.of("amount", "금액"));
            aliases.put("debitAmount", Set.of("출금", "출금(원)", "출금금액", "출금금액(원)"));
            aliases.put("creditAmount", Set.of("입금", "입금(원)", "입금금액", "입금금액(원)"));
            aliases.put("accountName", Set.of("account", "계좌", "계좌명"));
            aliases.put("categoryName", Set.of("category", "카테고리"));
            aliases.put("paymentName", Set.of("payment", "결제수단"));
            aliases.put("memo", Set.of("memo", "메모", "거래점"));
            aliases.put("installmentCount", Set.of("installmentcount", "할부개월", "할부", "installment"));
            aliases.put("isInterestFree", Set.of("isinterestfree", "면제", "이자면제"));

            return new HeaderImportProfile(
                40,
                2,
                aliases,
                List.of("tradingAt", "tradingTime", "title", "amount", "debitAmount", "creditAmount", "accountName",
                        "categoryName", "paymentName", "memo", "installmentCount", "isInterestFree")
            );
        }

        private int headerScanLimit () {
            return headerScanLimit;
        }

        private int headerMatchThreshold () {
            return headerMatchThreshold;
        }

        private Set<String> aliasesFor (String field) {
            return Optional.ofNullable(aliases.get(field))
                           .orElse(Set.of());
        }

        private List<String> fieldMatchOrder () {
            return fieldMatchOrder;
        }
    }
}
