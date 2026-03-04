package com.moneylog_backend.moneylog.transaction.service;

import java.io.BufferedReader;
import java.io.IOException;
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
    private static final int HEADER_SCAN_LIMIT = 40;
    private static final int HEADER_MATCH_THRESHOLD = 2;
    private static final Set<String> HEADER_TRADING_AT_TOKENS = Set.of("거래일", "거래일자", "일자", "날짜", "date");
    private static final Set<String> HEADER_TRADING_TIME_TOKENS = Set.of("거래시간", "시간");
    private static final Set<String> HEADER_TITLE_TOKENS = Set.of("title", "제목", "내용", "적요");
    private static final Set<String> HEADER_AMOUNT_TOKENS = Set.of("amount", "금액", "출금", "출금(원)", "입금", "입금(원)");
    private static final Set<String> HEADER_ACCOUNT_TOKENS = Set.of("account", "계좌", "계좌명");
    private static final Set<String> HEADER_CATEGORY_TOKENS = Set.of("category", "카테고리");
    private static final Set<String> HEADER_PAYMENT_TOKENS = Set.of("payment", "결제수단");
    private static final Set<String> HEADER_MEMO_TOKENS = Set.of("memo", "메모");
    private static final Set<String> HEADER_INSTALLMENT_TOKENS = Set.of("installmentcount", "할부개월", "할부", "installment");
    private static final Set<String> HEADER_INTEREST_FREE_TOKENS = Set.of("isinterestfree", "면제", "이자면제");
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
        int startRow = headerRowIndex >= 0 ? headerRowIndex + 1 : 0;

        int totalRows = 0;
        int resolvedRows = 0;
        int unresolvedRows = 0;
        int invalidRows = 0;
        Set<String> unresolvedAccounts = new TreeSet<>();
        Set<String> unresolvedCategories = new TreeSet<>();
        Set<String> unresolvedPayments = new TreeSet<>();
        List<TransactionImportPreviewRowDto> rows = new ArrayList<>();

        for (int i = startRow; i < rawRows.size(); i++) {
            List<String> rawRow = rawRows.get(i);
            if (isBlankRow(rawRow)) {
                continue;
            }

            TransactionImportPreviewRowDto previewRow = buildPreviewRow(
                i + 1,
                rawRow,
                headerIndex,
                accountLookup,
                categoryLookup,
                paymentLookup,
                unresolvedAccounts,
                unresolvedCategories,
                unresolvedPayments
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
            if (isHeaderToken(token, HEADER_TRADING_AT_TOKENS)) {
                found.put("tradingAt", i);
            } else if (isHeaderToken(token, HEADER_TITLE_TOKENS)) {
                found.put("title", i);
            } else if (isHeaderToken(token, Set.of("출금(원)", "출금", "입금(원)", "입금", "amount", "금액"))) {
                if (isHeaderToken(token, Set.of("출금(원)", "출금"))) {
                    found.put("debitAmount", i);
                } else if (isHeaderToken(token, Set.of("입금(원)", "입금"))) {
                    found.put("creditAmount", i);
                } else {
                    found.put("amount", i);
                }
            } else if (isHeaderToken(token, HEADER_AMOUNT_TOKENS)) {
                found.put("amount", i);
            } else if (isHeaderToken(token, HEADER_ACCOUNT_TOKENS)) {
                found.put("accountName", i);
            } else if (isHeaderToken(token, HEADER_CATEGORY_TOKENS)) {
                found.put("categoryName", i);
            } else if (isHeaderToken(token, HEADER_PAYMENT_TOKENS)) {
                found.put("paymentName", i);
            } else if (isHeaderToken(token, HEADER_MEMO_TOKENS)) {
                found.put("memo", i);
            } else if (isHeaderToken(token, HEADER_INSTALLMENT_TOKENS)) {
                found.put("installmentCount", i);
            } else if (isHeaderToken(token, HEADER_INTEREST_FREE_TOKENS)) {
                found.put("isInterestFree", i);
            } else if (isHeaderToken(token, HEADER_TRADING_TIME_TOKENS)) {
                found.put("tradingTime", i);
            }
        }
        return found;
    }

    private int resolveHeaderRowIndex (List<List<String>> rawRows) {
        int bestRow = -1;
        int bestScore = 0;

        int maxScan = Math.min(rawRows.size(), HEADER_SCAN_LIMIT);
        for (int i = 0; i < maxScan; i++) {
            List<String> row = rawRows.get(i);
            int score = headerRowScore(row);
            if (score >= HEADER_MATCH_THRESHOLD && score > bestScore) {
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
            if (isHeaderToken(token, HEADER_TRADING_AT_TOKENS)) {
                matchedFields.add("tradingAt");
            } else if (isHeaderToken(token, HEADER_TRADING_TIME_TOKENS)) {
                matchedFields.add("tradingTime");
            } else if (isHeaderToken(token, HEADER_TITLE_TOKENS)) {
                matchedFields.add("title");
            } else if (isHeaderToken(token, HEADER_AMOUNT_TOKENS)) {
                if (isHeaderToken(token, Set.of("출금(원)", "출금", "입금(원)", "입금"))) {
                    matchedFields.add("amount");
                } else {
                    matchedFields.add("amount");
                }
            } else if (isHeaderToken(token, HEADER_ACCOUNT_TOKENS)) {
                matchedFields.add("account");
            } else if (isHeaderToken(token, HEADER_CATEGORY_TOKENS)) {
                matchedFields.add("category");
            } else if (isHeaderToken(token, HEADER_PAYMENT_TOKENS)) {
                matchedFields.add("payment");
            } else if (isHeaderToken(token, HEADER_MEMO_TOKENS)) {
                matchedFields.add("memo");
            } else if (isHeaderToken(token, HEADER_INSTALLMENT_TOKENS)) {
                matchedFields.add("installment");
            } else if (isHeaderToken(token, HEADER_INTEREST_FREE_TOKENS)) {
                matchedFields.add("isInterestFree");
            }
        }
        if (matchedFields.size() >= 2) {
            return matchedFields.size();
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
                                                           Set<String> unresolvedPayments) {
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
        int columnInstallmentCount = headerIndex.getOrDefault("installmentCount", DEFAULT_COLUMN_ORDER.get("installmentCount"));
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
        String installmentCountRaw = getCell(rawRow, columnInstallmentCount);
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
        if (amount == null) {
            Integer debitAmount = parsePositiveAmount(debitAmountRaw);
            Integer creditAmount = parsePositiveAmount(creditAmountRaw);
            if (debitAmount != null && creditAmount != null) {
                errors.add("출금(원)과 입금(원) 값이 모두 있어 금액을 판별할 수 없습니다.");
                amount = debitAmount;
            } else if (debitAmount != null) {
                amount = debitAmount;
            } else if (creditAmount != null) {
                amount = creditAmount;
            }
        }
        if (amount == null) {
            errors.add("금액은 숫자(1 이상)로 입력해야 합니다.");
        }

        if (title == null || title.isBlank()) {
            title = "거래";
        }
        Integer installmentCount = parseNonNegativeInteger(installmentCountRaw);
        if (installmentCountRaw != null && !installmentCountRaw.isBlank() && (installmentCount == null || installmentCount < 2)) {
            errors.add("할부개월 수는 2 이상이어야 합니다.");
        }

        Boolean isInterestFree = parseBoolean(isInterestFreeRaw);

        Integer resolvedAccountId = null;
        if (accountName == null || accountName.isBlank()) {
            unresolvedFields.add("accountName");
            unresolvedAccounts.add(UNMAPPED_REFERENCE_LABEL);
        } else {
            List<AccountEntity> candidates = accountLookup.get(normalize(accountName));
            if (candidates == null || candidates.isEmpty()) {
                unresolvedFields.add("accountName");
                unresolvedAccounts.add(accountName);
            } else if (candidates.size() > 1) {
                unresolvedFields.add("accountName");
                unresolvedAccounts.add(accountName);
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
        } else {
            List<CategoryEntity> candidates = categoryLookup.get(normalize(categoryName));
            if (candidates == null || candidates.isEmpty()) {
                unresolvedFields.add("categoryName");
                unresolvedCategories.add(categoryName);
            } else if (candidates.size() > 1) {
                unresolvedFields.add("categoryName");
                unresolvedCategories.add(categoryName);
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
                errors.add("비용 카테고리는 결제수단이 필요합니다.");
            } else {
                List<PaymentEntity> candidates = paymentLookup.get(normalize(paymentName));
                if (candidates == null || candidates.isEmpty()) {
                    unresolvedFields.add("paymentName");
                    unresolvedPayments.add(paymentName);
                } else if (candidates.size() > 1) {
                    unresolvedFields.add("paymentName");
                    unresolvedPayments.add(paymentName);
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

    private boolean isHeaderToken (String normalizedToken, Set<String> candidateTokens) {
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

    private Integer parseNonNegativeInteger (String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.replaceAll(",", "").replaceAll("\\s+", "");
        try {
            Integer value = Integer.parseInt(normalized);
            return value >= 0 ? value : null;
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
}
