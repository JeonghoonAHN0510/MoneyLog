package com.moneylog_backend.moneylog.transaction.service;

import java.time.LocalDate;
import java.util.List;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.category.mapper.CategoryMapper;
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
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryMapper categoryMapper;
    private final TransactionMapper transactionMapper;

    @Transactional
    public int saveTransaction (TransactionDto transactionDto, Integer userId) {
        AccountEntity accountEntity = getAccountByTransactionDto(transactionDto.getAccountId(), userId);

        // todo categoryId + userId를 통해서 카테고리 유효성 검사 추가 필요
        CategoryEnum type = transactionDto.getCategoryType();
        if (type == null) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다.");
        }

        if ("EXPENSE".equals(type.name())) {
            // todo payment + userId를 통해서 결제수단 유효성 검사 추가 필요
            Integer paymentId = transactionDto.getPaymentId();
            if (paymentId != null && !paymentRepository.existsById(paymentId)) {
                throw new IllegalArgumentException("유효하지 않은 결제 수단입니다.");
            }
            accountEntity.withdraw(transactionDto.getAmount());
        } else if ("INCOME".equals(type.name())) {
            accountEntity.deposit(transactionDto.getAmount());
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
        TransactionEntity transactionEntity = getTransactionByTransactionDto(transactionDto);

        AccountEntity oldAccount = getAccountByTransactionDto(transactionEntity.getAccountId(), transactionEntity.getUserId());
        String oldType = categoryMapper.getCategoryTypeByCategoryId(transactionEntity.getCategoryId());

        updateAccountBalance(oldAccount, oldType, transactionEntity.getAmount(), true);

        Integer newAccountId = transactionDto.getAccountId();
        Integer newCategoryId = transactionDto.getCategoryId();
        AccountEntity newAccount = getAccountByTransactionDto(newAccountId, userId);
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
        TransactionEntity transactionEntity = getTransactionByTransactionDto(transactionDto);

        transactionRepository.delete(transactionEntity);
        return true;
    }

    private TransactionEntity getTransactionByTransactionDto (TransactionDto transactionDto) {
        TransactionEntity transactionEntity = transactionRepository.findById(transactionDto.getTransactionId())
                                                                   .orElseThrow(
                                                        () -> new IllegalArgumentException("존재하지 않는 지출 내역입니다."));
        if (!transactionDto.getUserId().equals(transactionEntity.getUserId())) {
            throw new AccessDeniedException("본인의 지출 내역이 아닙니다.");
        }

        return transactionEntity;
    }

    private AccountEntity getAccountByTransactionDto (Integer accountId, Integer userId) {
        AccountEntity accountEntity = accountRepository.findById(accountId)
                                                       .orElseThrow(
                                                           () -> new IllegalArgumentException("존재하지 않는 계좌입니다."));
        if (!accountEntity.getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 계좌가 아닙니다.");
        }

        return accountEntity;
    }
}