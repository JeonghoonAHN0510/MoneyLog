package com.moneylog_backend.moneylog.transaction.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;
import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;
import com.moneylog_backend.moneylog.payment.repository.PaymentRepository;
import com.moneylog_backend.moneylog.transaction.dto.req.TransactionImportCommitRequest;
import com.moneylog_backend.moneylog.transaction.dto.req.TransactionImportCommitRowDto;
import com.moneylog_backend.moneylog.transaction.dto.req.TransactionReqDto;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportCommitResponse;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportPreviewResponse;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportPreviewRowDto;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportReferenceDto;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportSummaryDto;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportUnresolvedIssueDto;
import com.moneylog_backend.moneylog.transaction.importer.FileParsingService;
import com.moneylog_backend.moneylog.transaction.importer.HeaderProfileResolver;
import com.moneylog_backend.moneylog.transaction.importer.HeaderProfileResolver.HeaderResolution;
import com.moneylog_backend.moneylog.transaction.importer.TransactionDataMapper;
import com.moneylog_backend.moneylog.transaction.importer.TransactionImportCommitValidator;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionImportService {
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionService transactionService;
    private final FileParsingService fileParsingService;
    private final HeaderProfileResolver headerProfileResolver;
    private final TransactionDataMapper transactionDataMapper;
    private final TransactionImportCommitValidator transactionImportCommitValidator;

    @Transactional(readOnly = true)
    public TransactionImportPreviewResponse previewImport (MultipartFile file, Integer userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        fileParsingService.validateFileSize(file);

        String fileName = fileParsingService.safeFileName(file.getOriginalFilename()).toLowerCase();
        List<List<String>> rawRows = fileParsingService.parseRowsByFile(fileName, file);
        if (rawRows.isEmpty()) {
            throw new IllegalArgumentException("임포트 대상 데이터가 없습니다.");
        }

        List<AccountEntity> accounts = accountRepository.findByUserId(userId);
        List<CategoryEntity> categories = categoryRepository.findByUserId(userId);
        List<PaymentEntity> payments = paymentRepository.findByUserId(userId);

        Map<String, List<AccountEntity>> accountLookup = buildLookupByName(accounts);
        Map<String, List<CategoryEntity>> categoryLookup = buildLookupByName(categories);
        Map<String, List<PaymentEntity>> paymentLookup = buildLookupByName(payments);

        HeaderResolution headerResolution = headerProfileResolver.resolve(rawRows);

        int totalRows = 0;
        int resolvedRows = 0;
        int unresolvedRows = 0;
        int invalidRows = 0;
        Set<String> unresolvedAccounts = new TreeSet<>();
        Set<String> unresolvedCategories = new TreeSet<>();
        Set<String> unresolvedPayments = new TreeSet<>();
        List<TransactionImportUnresolvedIssueDto> unresolvedIssues = new ArrayList<>();
        List<TransactionImportPreviewRowDto> rows = new ArrayList<>();

        for (int i = headerResolution.startRow(); i < rawRows.size(); i++) {
            List<String> rawRow = rawRows.get(i);
            if (transactionDataMapper.isBlankRow(rawRow)) {
                continue;
            }

            TransactionImportPreviewRowDto previewRow = transactionDataMapper.buildPreviewRow(
                i - headerResolution.startRow() + 1,
                rawRow,
                headerResolution.headerIndex(),
                accountLookup,
                categoryLookup,
                paymentLookup,
                unresolvedAccounts,
                unresolvedCategories,
                unresolvedPayments,
                headerResolution.headerLabelByField(),
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
            transactionImportCommitValidator.validateCommitRow(row, userId);
            TransactionReqDto txReq = row.toTransactionReqDto();
            createdTransactionIds.add(transactionService.saveTransaction(txReq, userId));
        }

        return TransactionImportCommitResponse.builder()
                                              .requestedCount(request.getRows().size())
                                              .importedCount(createdTransactionIds.size())
                                              .transactionIds(createdTransactionIds)
                                              .build();
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
}
