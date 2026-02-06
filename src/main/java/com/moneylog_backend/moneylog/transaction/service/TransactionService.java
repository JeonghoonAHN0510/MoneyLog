package com.moneylog_backend.moneylog.transaction.service;

import java.time.LocalDate;
import java.util.List;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.category.mapper.CategoryMapper;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;
import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;
import com.moneylog_backend.moneylog.transaction.dto.TransactionDto;
import com.moneylog_backend.moneylog.transaction.dto.query.SelectTransactionByUserIdQuery;
import com.moneylog_backend.moneylog.transaction.entity.TransactionEntity;
import com.moneylog_backend.moneylog.transaction.mapper.TransactionMapper;
import com.moneylog_backend.moneylog.transaction.repository.TransactionRepository;
import com.moneylog_backend.moneylog.payment.repository.PaymentRepository;

import org.springframework.security.access.AccessDeniedException;
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
    public int saveTransaction (TransactionDto transactionDto, Integer userId) {
        AccountEntity accountEntity = getAccountByIdAndValidateOwnership(transactionDto.getAccountId(), userId);

        CategoryEntity categoryEntity = getCategoryByIdAndValidateOwnership(transactionDto.getCategoryId(), userId);
        CategoryEnum type = transactionDto.getCategoryType();
        if (!categoryEntity.getType().equals(type)) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다.");
        }

        Integer amount = transactionDto.getAmount();
        if ("EXPENSE".equals(type.name())) {
            validatePaymentOwnership(transactionDto.getPaymentId(), userId);
            accountEntity.withdraw(amount);
        } else if ("INCOME".equals(type.name())) {
            accountEntity.deposit(amount);
        }

        TransactionEntity transactionEntity = transactionDto.toEntity(userId);
        transactionRepository.save(transactionEntity);

        return transactionEntity.getTransactionId();
    }

    public List<TransactionDto> getTransactionsByUserId (int userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.withDayOfMonth(1);

        SelectTransactionByUserIdQuery selectQuery = SelectTransactionByUserIdQuery.builder()
                                                                                   .userId(userId)
                                                                                   .startDate(startDate)
                                                                                   .endDate(endDate)
                                                                                   .build();
        return transactionMapper.getTransactionsByUserId(selectQuery);
    }

    @Transactional
    public TransactionDto updateTransaction(TransactionDto transactionDto, Integer userId) {
        TransactionEntity transactionEntity = getTransactionByDtoAndValidateOwnership(transactionDto);

        AccountEntity oldAccount = getAccountByIdAndValidateOwnership(transactionEntity.getAccountId(), transactionEntity.getUserId());
        String oldType = categoryMapper.getCategoryTypeByCategoryId(transactionEntity.getCategoryId());

        updateAccountBalance(oldAccount, oldType, transactionEntity.getAmount(), true);

        Integer newAccountId = transactionDto.getAccountId();
        Integer newCategoryId = transactionDto.getCategoryId();
        AccountEntity newAccount = getAccountByIdAndValidateOwnership(newAccountId, userId);
        String newType = categoryMapper.getCategoryTypeByCategoryId(newCategoryId);

        if (newType == null) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다.");
        }

        Integer newPaymentId = transactionDto.getPaymentId();
        if (newPaymentId != null && !paymentRepository.existsById(newPaymentId)) {
            throw new IllegalArgumentException("유효하지 않은 결제 수단입니다.");
        }

        Integer newAmount = transactionDto.getAmount();
        updateAccountBalance(newAccount, newType, newAmount, false);

        transactionEntity.update(
            newCategoryId,
            newPaymentId,
            newAccountId,
            transactionDto.getTitle(),
            newAmount,
            transactionDto.getMemo(),
            transactionDto.getTradingAt()
        );

        return transactionEntity.toDto();
    }

    /**
     * 계좌 잔액 업데이트 헬퍼 메서드
     * @param account 계좌 엔티티
     * @param type 수입/지출 타입 ("INCOME" or "EXPENSE")
     * @param amount 금액
     * @param isRevert true면 기존 내역 취소(원복), false면 신규 반영
     */
    private void updateAccountBalance(AccountEntity account, String type, int amount, boolean isRevert) {
        boolean isExpense = "EXPENSE".equals(type);
        boolean isIncome = "INCOME".equals(type);

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
    public boolean deleteTransaction (TransactionDto transactionDto) {
        TransactionEntity transactionEntity = getTransactionByDtoAndValidateOwnership(transactionDto);

        transactionRepository.delete(transactionEntity);
        return true;
    }

    private TransactionEntity getTransactionByDtoAndValidateOwnership (TransactionDto transactionDto) {
        TransactionEntity transactionEntity = transactionRepository.findById(transactionDto.getTransactionId())
                                                                   .orElseThrow(
                                                        () -> new IllegalArgumentException("존재하지 않는 지출 내역입니다."));
        if (!transactionDto.getUserId().equals(transactionEntity.getUserId())) {
            throw new AccessDeniedException("본인의 지출 내역이 아닙니다.");
        }

        return transactionEntity;
    }

    private AccountEntity getAccountByIdAndValidateOwnership (Integer accountId, Integer userId) {
        AccountEntity accountEntity = accountRepository.findById(accountId)
                                                       .orElseThrow(
                                                           () -> new IllegalArgumentException("존재하지 않는 계좌입니다."));
        if (!accountEntity.getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 계좌가 아닙니다.");
        }

        return accountEntity;
    }

    private CategoryEntity getCategoryByIdAndValidateOwnership (Integer categoryId, Integer userId) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
                                                       .orElseThrow(
                                                           () -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
        if (!categoryEntity.getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 카테고리가 아닙니다.");
        }

        return categoryEntity;
    }

    private void validatePaymentOwnership (Integer paymentId, Integer userId) {
        PaymentEntity paymentEntity = paymentRepository.findById(paymentId)
                                                          .orElseThrow(
                                                              () -> new IllegalArgumentException("존재하지 않는 결제수단입니다."));
        if (!paymentEntity.getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 결제수단가 아닙니다.");
        }
    }
}