package com.moneylog_backend.moneylog.transaction.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.exception.ResourceNotFoundException;
import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.global.util.InputStringNormalizer;
import com.moneylog_backend.global.util.OwnershipValidator;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.account.service.AccountLockHelper;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.category.mapper.CategoryMapper;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;
import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;
import com.moneylog_backend.moneylog.payment.repository.PaymentRepository;
import com.moneylog_backend.moneylog.transaction.dto.query.SelectTransactionByUserIdQuery;
import com.moneylog_backend.moneylog.transaction.dto.req.TransactionReqDto;
import com.moneylog_backend.moneylog.transaction.dto.req.TransactionSearchReqDto;
import com.moneylog_backend.moneylog.transaction.dto.res.CategoryStatsResDto;
import com.moneylog_backend.moneylog.transaction.dto.res.DailySummaryResDto;
import com.moneylog_backend.moneylog.transaction.dto.res.DashboardResDto;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionResDto;
import com.moneylog_backend.moneylog.transaction.entity.TransactionEntity;
import com.moneylog_backend.moneylog.transaction.installment.entity.CardInstallmentPlanEntity;
import com.moneylog_backend.moneylog.transaction.installment.repository.CardInstallmentPlanRepository;
import com.moneylog_backend.moneylog.transaction.mapper.TransactionMapper;
import com.moneylog_backend.moneylog.transaction.repository.TransactionRepository;
import com.moneylog_backend.moneylog.transaction.validation.TransactionWriteValidator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final AccountLockHelper accountLockHelper;
    private final CardInstallmentPlanRepository installmentPlanRepository;
    private final TransactionMapper transactionMapper;
    private final CategoryMapper categoryMapper;
    private final TransactionSettlementService transactionSettlementService;
    private final TransactionWriteValidator transactionWriteValidator;

    @Transactional
    public int saveTransaction (TransactionReqDto transactionReqDto, Integer userId) {
        String normalizedTitle = InputStringNormalizer.trimToNull(transactionReqDto.getTitle());
        String normalizedMemo = InputStringNormalizer.trimNullable(transactionReqDto.getMemo());
        transactionWriteValidator.validateRequiredId(transactionReqDto.getAccountId(), "계좌 ID");
        transactionWriteValidator.validateRequiredId(transactionReqDto.getCategoryId(), "카테고리 ID");
        transactionWriteValidator.validateBasicFields(
            normalizedTitle,
            normalizedMemo,
            transactionReqDto.getAmount(),
            transactionReqDto.getTradingAt()
        );

        CategoryEntity categoryEntity = getCategoryByIdAndValidateOwnership(transactionReqDto.getCategoryId(), userId);
        CategoryEnum type = categoryEntity.getType();
        transactionWriteValidator.validatePaymentPolicy(type, transactionReqDto.getPaymentId());

        if (CategoryEnum.EXPENSE.equals(type)) {
            PaymentEntity paymentEntity = getPaymentByIdAndValidateOwnership(transactionReqDto.getPaymentId(), userId);

            if (transactionReqDto.isInstallment()) {
                transactionWriteValidator.validateInstallment(
                    type,
                    transactionReqDto.getInstallmentCount(),
                    paymentEntity.getType()
                );
                AccountEntity accountEntity = accountLockHelper.lockOwnedAccount(transactionReqDto.getAccountId(), userId);
                return saveInstallmentTransactions(transactionReqDto, categoryEntity, accountEntity, normalizedTitle,
                                                  normalizedMemo);
            }

            AccountEntity accountEntity = accountLockHelper.lockOwnedAccount(transactionReqDto.getAccountId(), userId);
            return saveSingleTransaction(transactionReqDto, userId, accountEntity, type, normalizedTitle, normalizedMemo);
        }

        if (CategoryEnum.INCOME.equals(type)) {
            if (transactionReqDto.isInstallment()) {
                transactionWriteValidator.validateInstallment(type, transactionReqDto.getInstallmentCount(), null);
            }
            AccountEntity accountEntity = accountLockHelper.lockOwnedAccount(transactionReqDto.getAccountId(), userId);
            return saveSingleTransaction(transactionReqDto, userId, accountEntity, type, normalizedTitle, normalizedMemo);
        }

        throw new IllegalStateException("지원하지 않는 카테고리 타입입니다.");
    }

    private int saveSingleTransaction (TransactionReqDto transactionReqDto, Integer userId, AccountEntity accountEntity,
                                       CategoryEnum type, String normalizedTitle, String normalizedMemo) {
        Integer amount = transactionReqDto.getAmount();
        updateAccountBalance(accountEntity, type, amount, false);

        TransactionEntity transactionEntity = transactionReqDto.toEntity(userId, normalizedTitle, normalizedMemo);
        transactionEntity.initializeDefaultSingleSettlementState(nowDateTimeKst());
        transactionRepository.save(transactionEntity);

        return transactionEntity.getTransactionId();
    }

    public List<TransactionResDto> getTransactionsByUserId (int userId) {
        LocalDate endDate = nowDateKst();
        LocalDate startDate = endDate.withDayOfMonth(1);

        SelectTransactionByUserIdQuery selectQuery = SelectTransactionByUserIdQuery.builder()
                                                                             .userId(userId)
                                                                             .startDate(startDate)
                                                                             .endDate(endDate)
                                                                             .build();
        return transactionMapper.getTransactionsByUserId(selectQuery);
    }

    @Transactional(readOnly = true)
    public List<TransactionResDto> searchTransactions (Integer userId, TransactionSearchReqDto searchDto) {
        searchDto.setUserId(userId);
        return transactionMapper.searchTransactions(searchDto);
    }

    @Transactional
    public TransactionResDto updateTransaction (TransactionReqDto transactionReqDto, Integer userId) {
        String normalizedTitle = InputStringNormalizer.trimToNull(transactionReqDto.getTitle());
        String normalizedMemo = InputStringNormalizer.trimNullable(transactionReqDto.getMemo());
        transactionWriteValidator.validateRequiredId(transactionReqDto.getTransactionId(), "거래 ID");
        transactionWriteValidator.validateRequiredId(transactionReqDto.getAccountId(), "계좌 ID");
        transactionWriteValidator.validateRequiredId(transactionReqDto.getCategoryId(), "카테고리 ID");
        transactionWriteValidator.validateBasicFields(
            normalizedTitle,
            normalizedMemo,
            transactionReqDto.getAmount(),
            transactionReqDto.getTradingAt()
        );

        TransactionEntity transactionEntity = getTransactionByIdAndValidateOwnershipForUpdate(
            transactionReqDto.getTransactionId(), userId);

        if (Boolean.TRUE.equals(transactionEntity.getIsInstallment())) {
            throw new IllegalArgumentException("할부 거래는 수정할 수 없습니다.");
        }

        String oldTypeCode = categoryMapper.getCategoryTypeByCategoryId(transactionEntity.getCategoryId());
        CategoryEnum oldType = CategoryEnum.fromCode(oldTypeCode);

        Integer newAccountId = transactionReqDto.getAccountId();
        Integer newCategoryId = transactionReqDto.getCategoryId();
        CategoryEntity newCategory = getCategoryByIdAndValidateOwnership(newCategoryId, userId);
        CategoryEnum newType = newCategory.getType();

        Integer newPaymentId = transactionReqDto.getPaymentId();
        transactionWriteValidator.validatePaymentPolicy(newType, newPaymentId);
        if (CategoryEnum.EXPENSE.equals(newType)) {
            getPaymentByIdAndValidateOwnership(newPaymentId, userId);
        }

        Map<Integer, AccountEntity> lockedAccounts = accountLockHelper.lockOwnedAccounts(
            List.of(transactionEntity.getAccountId(), newAccountId),
            userId
        );
        AccountEntity oldAccount = lockedAccounts.get(transactionEntity.getAccountId());
        AccountEntity newAccount = lockedAccounts.get(newAccountId);

        updateAccountBalance(oldAccount, oldType, transactionEntity.getAmount(), true);

        Integer newAmount = transactionReqDto.getAmount();
        updateAccountBalance(newAccount, newType, newAmount, false);

        transactionEntity.update(newCategoryId, newPaymentId, newAccountId, normalizedTitle, newAmount,
                                 normalizedMemo, transactionReqDto.getTradingAt());

        return transactionEntity.toDto();
    }

    @Transactional
    public boolean deleteTransaction (Integer transactionId, Integer userId) {
        Integer installmentPlanId = getInstallmentPlanId(transactionId, userId);
        CardInstallmentPlanEntity installmentPlan = null;
        if (installmentPlanId != null) {
            installmentPlan = getInstallmentPlanByIdForUpdate(installmentPlanId);
        }
        TransactionEntity transactionEntity = getTransactionByIdAndValidateOwnershipForUpdate(transactionId, userId);

        Map<Integer, AccountEntity> lockedAccounts = accountLockHelper.lockOwnedAccounts(
            List.of(transactionEntity.getAccountId()),
            userId
        );
        AccountEntity accountEntity = lockedAccounts.get(transactionEntity.getAccountId());

        String categoryTypeCode = categoryMapper.getCategoryTypeByCategoryId(transactionEntity.getCategoryId());
        CategoryEnum categoryType = CategoryEnum.fromCode(categoryTypeCode);

        if (transactionEntity.getIsSettled() == null || Boolean.TRUE.equals(transactionEntity.getIsSettled())) {
            updateAccountBalance(accountEntity, categoryType, transactionEntity.getAmount(), true);
        }

        transactionRepository.delete(transactionEntity);

        if (installmentPlan != null) {
            transactionSettlementService.resyncInstallmentPlanProgress(installmentPlan);
        }

        return true;
    }

    public List<DailySummaryResDto> getCalendarData (Integer userId, int year, int month) {
        SelectTransactionByUserIdQuery query = createMonthlyQuery(userId, year, month);

        return transactionMapper.getDailySummaries(query);
    }

    public DashboardResDto getDashboardData (Integer userId, int year, int month) {
        SelectTransactionByUserIdQuery query = createMonthlyQuery(userId, year, month);

        List<DailySummaryResDto> dailySummaries = transactionMapper.getDailySummaries(query);
        long totalIncome = dailySummaries.stream().mapToLong(DailySummaryResDto::getTotalIncome).sum();
        long totalExpense = dailySummaries.stream().mapToLong(DailySummaryResDto::getTotalExpense).sum();
        long totalBalance = totalIncome - totalExpense;

        List<CategoryStatsResDto> categoryStats = transactionMapper.getCategoryStats(query);

        List<CategoryStatsResDto> calculatedCategoryStats = categoryStats.stream().map(stat -> {
            double ratio = ( totalExpense > 0 ) ? (double) stat.getTotalAmount() / totalExpense * 100 : 0.0;
            return CategoryStatsResDto.builder()
                                      .categoryName(stat.getCategoryName())
                                      .totalAmount(stat.getTotalAmount())
                                      .ratio(ratio)
                                      .build();
        }).collect(Collectors.toList());

        return DashboardResDto.builder()
                              .totalIncome(totalIncome)
                              .totalExpense(totalExpense)
                              .totalBalance(totalBalance)
                              .categoryStats(calculatedCategoryStats)
                              .build();
    }

    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    public void settleOverdueInstallments () {
        List<CardInstallmentPlanEntity> plans = installmentPlanRepository
            .findByIsActiveTrueAndIsCompletedFalseAndFirstTradingAtLessThanEqual(nowDateKst());
        if (plans.isEmpty()) {
            return;
        }

        for (CardInstallmentPlanEntity plan : plans) {
            try {
                transactionSettlementService.settleInstallmentPlan(plan.getInstallmentPlanId());
            } catch (Exception e) {
                log.error("할부 자동 정산 실패: installmentPlanId={}", plan.getInstallmentPlanId(), e);
            }
        }
    }

    private int saveInstallmentTransactions (TransactionReqDto transactionReqDto, CategoryEntity categoryEntity,
                                            AccountEntity accountEntity, String normalizedTitle, String normalizedMemo) {
        Integer installmentCount = transactionReqDto.getInstallmentCount();
        boolean isInterestFree = Boolean.TRUE.equals(transactionReqDto.getIsInterestFree());
        LocalDate firstTradingAt = transactionReqDto.getTradingAt();
        LocalDate today = nowDateKst();

        CardInstallmentPlanEntity plan = CardInstallmentPlanEntity.builder()
                                                               .userId(categoryEntity.getUserId())
                                                               .categoryId(categoryEntity.getCategoryId())
                                                               .paymentId(transactionReqDto.getPaymentId())
                                                               .accountId(transactionReqDto.getAccountId())
                                                               .title(normalizedTitle)
                                                               .memo(normalizedMemo)
                                                               .totalAmount(transactionReqDto.getAmount())
                                                               .installmentCount(installmentCount)
                                                               .firstTradingAt(firstTradingAt)
                                                               .isInterestFree(isInterestFree)
                                                               .isActive(true)
                                                               .settledCount(0)
                                                               .isCompleted(false)
                                                               .build();
        installmentPlanRepository.save(plan);

        Integer firstTransactionId = null;
        int settledCount = 0;
        for (int i = 0; i < installmentCount; i++) {
            Integer installmentAmount = calculateInstallmentAmount(i, transactionReqDto.getAmount(), installmentCount);
            LocalDate tradingAt = firstTradingAt.plusMonths(i);
            boolean settled = !tradingAt.isAfter(today);

            TransactionEntity transactionEntity = TransactionEntity.builder()
                                                                .userId(categoryEntity.getUserId())
                                                                .categoryId(categoryEntity.getCategoryId())
                                                                .paymentId(transactionReqDto.getPaymentId())
                                                                .accountId(transactionReqDto.getAccountId())
                                                                .title(normalizedTitle)
                                                                .amount(installmentAmount)
                                                                .memo(normalizedMemo)
                                                                .tradingAt(tradingAt)
                                                                .installmentPlanId(plan.getInstallmentPlanId())
                                                                .installmentNo(i + 1)
                                                                .installmentTotalCount(installmentCount)
                                                                .isInstallment(true)
                                                                .isInterestFree(isInterestFree)
                                                                .isSettled(settled)
                                                                .settledAt(settled ? nowDateTimeKst() : null)
                                                                .build();

            transactionRepository.save(transactionEntity);
            if (firstTransactionId == null) {
                firstTransactionId = transactionEntity.getTransactionId();
            }

            if (settled) {
                settledCount++;
                accountEntity.withdraw(transactionEntity.getAmount());
            }
        }

        plan.initializeProgress(settledCount, settledCount > 0 ? nowDateTimeKst() : null);

        return firstTransactionId;
    }

    private LocalDate nowDateKst () {
        return LocalDate.now(KST_ZONE);
    }

    private LocalDateTime nowDateTimeKst () {
        return LocalDateTime.now(KST_ZONE);
    }

    private Integer calculateInstallmentAmount (int index, Integer totalAmount, Integer installmentCount) {
        int baseAmount = totalAmount / installmentCount;
        int remainder = totalAmount % installmentCount;

        return baseAmount + (index < remainder ? 1 : 0);
    }

    private void updateAccountBalance (AccountEntity account, CategoryEnum type, int amount, boolean isRevert) {
        boolean isExpense = CategoryEnum.EXPENSE.equals(type);
        boolean isIncome = CategoryEnum.INCOME.equals(type);

        if (isRevert) {
            if (isExpense) {
                account.deposit(amount);
            } else if (isIncome) {
                account.withdraw(amount);
            }
        } else {
            if (isExpense) {
                account.withdraw(amount);
            } else if (isIncome) {
                account.deposit(amount);
            }
        }
    }

    private Integer getInstallmentPlanId (Integer transactionId, Integer userId) {
        return getTransactionByIdAndValidateOwnership(transactionId, userId).getInstallmentPlanId();
    }

    private TransactionEntity getTransactionByIdAndValidateOwnership (Integer transactionId, Integer userId) {
        TransactionEntity transactionEntity = transactionRepository.findById(transactionId)
                                                               .orElseThrow(() -> new ResourceNotFoundException(
                                                                   ErrorMessageConstants.TRANSACTION_NOT_FOUND));
        OwnershipValidator.validateOwner(transactionEntity.getUserId(), userId, "본인의 지출 내역이 아닙니다.");

        return transactionEntity;
    }

    private TransactionEntity getTransactionByIdAndValidateOwnershipForUpdate (Integer transactionId, Integer userId) {
        TransactionEntity transactionEntity = transactionRepository.findByIdForUpdate(transactionId)
                                                                   .orElseThrow(
                                                                       () -> new ResourceNotFoundException(
                                                                           ErrorMessageConstants.TRANSACTION_NOT_FOUND));
        OwnershipValidator.validateOwner(transactionEntity.getUserId(), userId, "본인의 지출 내역이 아닙니다.");

        return transactionEntity;
    }

    private CardInstallmentPlanEntity getInstallmentPlanByIdForUpdate (Integer installmentPlanId) {
        return installmentPlanRepository.findByIdForUpdate(installmentPlanId)
                                        .orElseThrow(
                                            () -> new IllegalArgumentException("할부 계획을 찾을 수 없습니다."));
    }

    private AccountEntity getAccountByIdAndValidateOwnership (Integer accountId, Integer userId) {
        AccountEntity accountEntity = accountRepository.findById(accountId)
                                                       .orElseThrow(
                                                           () -> new ResourceNotFoundException(
                                                               ErrorMessageConstants.ACCOUNT_NOT_FOUND));
        OwnershipValidator.validateOwner(accountEntity.getUserId(), userId, "본인의 계좌가 아닙니다.");

        return accountEntity;
    }

    private CategoryEntity getCategoryByIdAndValidateOwnership (Integer categoryId, Integer userId) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
                                                          .orElseThrow(
                                                              () -> new ResourceNotFoundException(
                                                                  ErrorMessageConstants.CATEGORY_NOT_FOUND));
        OwnershipValidator.validateOwner(categoryEntity.getUserId(), userId, "본인의 카테고리가 아닙니다.");

        return categoryEntity;
    }

    private PaymentEntity getPaymentByIdAndValidateOwnership (Integer paymentId, Integer userId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("결제수단 ID는 필수입니다.");
        }

        PaymentEntity paymentEntity = paymentRepository.findById(paymentId)
                                                   .orElseThrow(
                                                       () -> new ResourceNotFoundException(
                                                           ErrorMessageConstants.PAYMENT_NOT_FOUND));
        OwnershipValidator.validateOwner(paymentEntity.getUserId(), userId, "본인의 결제수단이 아닙니다.");

        return paymentEntity;
    }

    private SelectTransactionByUserIdQuery createMonthlyQuery (Integer userId, Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return SelectTransactionByUserIdQuery.builder().userId(userId).startDate(startDate).endDate(endDate).build();
    }
}
