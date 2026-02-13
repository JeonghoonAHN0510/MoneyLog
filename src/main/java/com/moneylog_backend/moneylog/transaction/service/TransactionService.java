package com.moneylog_backend.moneylog.transaction.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.exception.ResourceNotFoundException;
import com.moneylog_backend.global.util.OwnershipValidator;
import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
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
import com.moneylog_backend.moneylog.transaction.mapper.TransactionMapper;
import com.moneylog_backend.moneylog.transaction.repository.TransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final CategoryMapper categoryMapper;

    @Transactional
    public int saveTransaction (TransactionReqDto transactionReqDto, Integer userId) {
        AccountEntity accountEntity = getAccountByIdAndValidateOwnership(transactionReqDto.getAccountId(), userId);

        CategoryEntity categoryEntity = getCategoryByIdAndValidateOwnership(transactionReqDto.getCategoryId(), userId);
        CategoryEnum type = categoryEntity.getType();

        Integer amount = transactionReqDto.getAmount();
        if (CategoryEnum.EXPENSE.equals(type)) {
            validatePaymentOwnership(transactionReqDto.getPaymentId(), userId);
            accountEntity.withdraw(amount);
        } else if (CategoryEnum.INCOME.equals(type)) {
            accountEntity.deposit(amount);
        }

        TransactionEntity transactionEntity = transactionReqDto.toEntity(userId);
        transactionRepository.save(transactionEntity);

        return transactionEntity.getTransactionId();
    }

    public List<TransactionResDto> getTransactionsByUserId (int userId) {
        LocalDate endDate = LocalDate.now();
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
        TransactionEntity transactionEntity = getTransactionByIdAndValidateOwnership(
            transactionReqDto.getTransactionId(), userId);

        AccountEntity oldAccount = getAccountByIdAndValidateOwnership(transactionEntity.getAccountId(),
                                                                      transactionEntity.getUserId());
        String oldTypeCode = categoryMapper.getCategoryTypeByCategoryId(transactionEntity.getCategoryId());
        CategoryEnum oldType = CategoryEnum.fromCode(oldTypeCode);

        updateAccountBalance(oldAccount, oldType, transactionEntity.getAmount(), true);

        Integer newAccountId = transactionReqDto.getAccountId();
        Integer newCategoryId = transactionReqDto.getCategoryId();
        AccountEntity newAccount = getAccountByIdAndValidateOwnership(newAccountId, userId);
        CategoryEntity newCategory = getCategoryByIdAndValidateOwnership(newCategoryId, userId);
        CategoryEnum newType = newCategory.getType();

        Integer newPaymentId = transactionReqDto.getPaymentId();
        if (CategoryEnum.EXPENSE.equals(newType)) {
            validatePaymentOwnership(newPaymentId, userId);
        } else {
            newPaymentId = null;
        }

        Integer newAmount = transactionReqDto.getAmount();
        updateAccountBalance(newAccount, newType, newAmount, false);

        transactionEntity.update(newCategoryId, newPaymentId, newAccountId, transactionReqDto.getTitle(), newAmount,
                                 transactionReqDto.getMemo(), transactionReqDto.getTradingAt());

        return transactionEntity.toDto();
    }

    /**
     * 계좌 잔액 업데이트 헬퍼 메서드
     *
     * @param account  계좌 엔티티
     * @param type     수입/지출 타입
     * @param amount   금액
     * @param isRevert true면 기존 내역 취소(원복), false면 신규 반영
     */
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

    @Transactional
    public boolean deleteTransaction (Integer transactionId, Integer userId) {
        TransactionEntity transactionEntity = getTransactionByIdAndValidateOwnership(transactionId, userId);

        AccountEntity accountEntity = getAccountByIdAndValidateOwnership(transactionEntity.getAccountId(), userId);
        String categoryTypeCode = categoryMapper.getCategoryTypeByCategoryId(transactionEntity.getCategoryId());
        CategoryEnum categoryType = CategoryEnum.fromCode(categoryTypeCode);
        updateAccountBalance(accountEntity, categoryType, transactionEntity.getAmount(), true);

        transactionRepository.delete(transactionEntity);
        return true;
    }

    public List<DailySummaryResDto> getCalendarData (Integer userId, int year, int month) {
        SelectTransactionByUserIdQuery query = createMonthlyQuery(userId, year, month);

        return transactionMapper.getDailySummaries(query);
    }

    public DashboardResDto getDashboardData (Integer userId, int year, int month) {
        SelectTransactionByUserIdQuery query = createMonthlyQuery(userId, year, month);

        // 1. 일별 합계 조회하여 전체 수입/지출 계산
        List<DailySummaryResDto> dailySummaries = transactionMapper.getDailySummaries(query);
        long totalIncome = dailySummaries.stream().mapToLong(DailySummaryResDto::getTotalIncome).sum();
        long totalExpense = dailySummaries.stream().mapToLong(DailySummaryResDto::getTotalExpense).sum();
        long totalBalance = totalIncome - totalExpense;

        // 2. 카테고리별 지출 통계 조회
        List<CategoryStatsResDto> categoryStats = transactionMapper.getCategoryStats(query);

        // 3. 비율 계산
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

    private TransactionEntity getTransactionByIdAndValidateOwnership (Integer transactionId, Integer userId) {
        TransactionEntity transactionEntity = transactionRepository.findById(transactionId)
                                                                   .orElseThrow(() -> new ResourceNotFoundException(
                                                                       ErrorMessageConstants.TRANSACTION_NOT_FOUND));
        OwnershipValidator.validateOwner(transactionEntity.getUserId(), userId, "본인의 지출 내역이 아닙니다.");

        return transactionEntity;
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

    private void validatePaymentOwnership (Integer paymentId, Integer userId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("결제수단 ID는 필수입니다.");
        }

        PaymentEntity paymentEntity = paymentRepository.findById(paymentId)
                                                       .orElseThrow(
                                                           () -> new ResourceNotFoundException(
                                                               ErrorMessageConstants.PAYMENT_NOT_FOUND));
        OwnershipValidator.validateOwner(paymentEntity.getUserId(), userId, "본인의 결제수단이 아닙니다.");
    }

    private SelectTransactionByUserIdQuery createMonthlyQuery (Integer userId, Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return SelectTransactionByUserIdQuery.builder().userId(userId).startDate(startDate).endDate(endDate).build();
    }
}
