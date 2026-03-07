package com.moneylog_backend.moneylog.transaction.importer;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;
import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;
import com.moneylog_backend.moneylog.payment.repository.PaymentRepository;
import com.moneylog_backend.moneylog.transaction.dto.req.TransactionImportCommitRowDto;
import com.moneylog_backend.moneylog.transaction.validation.TransactionWriteValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionImportCommitValidator {
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionWriteValidator transactionWriteValidator;

    public void validateCommitRow (TransactionImportCommitRowDto row, Integer userId) {
        transactionWriteValidator.validateBasicFields(row.getTitle(), row.getMemo(), row.getAmount(), row.getTradingAt());
        transactionWriteValidator.validateRequiredId(row.getAccountId(), "계좌 ID");
        AccountEntity accountEntity = accountRepository.findById(row.getAccountId())
                                                       .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));
        if (!Objects.equals(accountEntity.getUserId(), userId)) {
            throw new IllegalArgumentException("권한이 없는 계좌입니다.");
        }
        transactionWriteValidator.validateRequiredId(row.getCategoryId(), "카테고리 ID");
        CategoryEntity categoryEntity = categoryRepository.findById(row.getCategoryId())
                                                          .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        if (!Objects.equals(categoryEntity.getUserId(), userId)) {
            throw new IllegalArgumentException("권한이 없는 카테고리입니다.");
        }
        transactionWriteValidator.validatePaymentPolicy(categoryEntity.getType(), row.getPaymentId());
        if (CategoryEnum.EXPENSE.equals(categoryEntity.getType())) {
            PaymentEntity paymentEntity = paymentRepository.findById(row.getPaymentId())
                                                           .orElseThrow(() -> new IllegalArgumentException("결제수단을 찾을 수 없습니다."));
            if (!Objects.equals(paymentEntity.getUserId(), userId)) {
                throw new IllegalArgumentException("권한이 없는 결제수단입니다.");
            }
        }
    }
}
