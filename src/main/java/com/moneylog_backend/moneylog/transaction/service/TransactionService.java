package com.moneylog_backend.moneylog.transaction.service;

import java.time.LocalDate;
import java.util.List;

import com.moneylog_backend.global.exception.ResourceNotFoundException;
import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.category.mapper.CategoryMapper;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;
import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;
import com.moneylog_backend.moneylog.transaction.dto.req.TransactionReqDto;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionResDto;
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
    public int saveTransaction(TransactionReqDto transactionReqDto, Integer userId) {
        AccountEntity accountEntity = getAccountByIdAndValidateOwnership(transactionReqDto.getAccountId(), userId);

        CategoryEntity categoryEntity = getCategoryByIdAndValidateOwnership(transactionReqDto.getCategoryId(), userId);
        CategoryEnum type = categoryEntity.getType();

        Integer amount = transactionReqDto.getAmount();
        if ("EXPENSE".equals(type.name())) {
            validatePaymentOwnership(transactionReqDto.getPaymentId(), userId);
            accountEntity.withdraw(amount);
        } else if ("INCOME".equals(type.name())) {
            accountEntity.deposit(amount);
        }

        TransactionEntity transactionEntity = transactionReqDto.toEntity(userId);
        transactionRepository.save(transactionEntity);

        return transactionEntity.getTransactionId();
    }

    public List<TransactionResDto> getTransactionsByUserId(int userId) {
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
    public TransactionResDto updateTransaction(TransactionReqDto transactionReqDto, Integer userId) {
        TransactionEntity transactionEntity = getTransactionByIdAndValidateOwnership(transactionReqDto.getTransactionId(), userId);

        AccountEntity oldAccount = getAccountByIdAndValidateOwnership(transactionEntity.getAccountId(),
                                                                      transactionEntity.getUserId());
        String oldType = categoryMapper.getCategoryTypeByCategoryId(transactionEntity.getCategoryId());

        updateAccountBalance(oldAccount, oldType, transactionEntity.getAmount(), true);

        Integer newAccountId = transactionReqDto.getAccountId();
        Integer newCategoryId = transactionReqDto.getCategoryId();
        AccountEntity newAccount = getAccountByIdAndValidateOwnership(newAccountId, userId);
        String newType = categoryMapper.getCategoryTypeByCategoryId(newCategoryId);

        if (newType == null) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다.");
        }

        Integer newPaymentId = transactionReqDto.getPaymentId();
        if (newPaymentId != null && !paymentRepository.existsById(newPaymentId)) {
            throw new IllegalArgumentException("유효하지 않은 결제 수단입니다.");
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
     * @param type     수입/지출 타입 ("INCOME" or "EXPENSE")
     * @param amount   금액
     * @param isRevert true면 기존 내역 취소(원복), false면 신규 반영
     */
    private void updateAccountBalance (AccountEntity account, String type, int amount, boolean isRevert) {
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
    public boolean deleteTransaction(Integer transactionId, Integer userId) {
        TransactionEntity transactionEntity = getTransactionByIdAndValidateOwnership(transactionId, userId);

        transactionRepository.delete(transactionEntity);
        return true;
    }

    private TransactionEntity getTransactionByIdAndValidateOwnership(Integer transactionId, Integer userId) {
        TransactionEntity transactionEntity = transactionRepository.findById(transactionId)
                                                                   .orElseThrow(() -> new ResourceNotFoundException(
                                                                       "존재하지 않는 지출 내역입니다."));
        if (!userId.equals(transactionEntity.getUserId())) {
            throw new AccessDeniedException("본인의 지출 내역이 아닙니다.");
        }

        return transactionEntity;
    }

    private AccountEntity getAccountByIdAndValidateOwnership (Integer accountId, Integer userId) {
        AccountEntity accountEntity = accountRepository.findById(accountId)
                                                       .orElseThrow(
                                                           () -> new ResourceNotFoundException("존재하지 않는 계좌입니다."));
        if (!accountEntity.getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 계좌가 아닙니다.");
        }

        return accountEntity;
    }

    private CategoryEntity getCategoryByIdAndValidateOwnership (Integer categoryId, Integer userId) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
                                                          .orElseThrow(
                                                              () -> new ResourceNotFoundException("존재하지 않는 카테고리입니다."));
        if (!categoryEntity.getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 카테고리가 아닙니다.");
        }

        return categoryEntity;
    }

    private void validatePaymentOwnership (Integer paymentId, Integer userId) {
        PaymentEntity paymentEntity = paymentRepository.findById(paymentId)
                                                       .orElseThrow(
                                                           () -> new ResourceNotFoundException("존재하지 않는 결제수단입니다."));
        if (!paymentEntity.getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 결제수단가 아닙니다.");
        }
    }
}