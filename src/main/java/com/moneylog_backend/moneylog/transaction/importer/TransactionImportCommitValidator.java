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
import com.moneylog_backend.moneylog.transaction.validation.TransactionCategoryPaymentRuleValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionImportCommitValidator {
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionCategoryPaymentRuleValidator transactionCategoryPaymentRuleValidator;

    public void validateCommitRow (TransactionImportCommitRowDto row, Integer userId) {
        if (row.getTitle() == null || row.getTitle().isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (row.getTitle().length() > 100) {
            throw new IllegalArgumentException("제목은 100자 이내여야 합니다.");
        }
        if (row.getMemo() != null && row.getMemo().length() > 500) {
            throw new IllegalArgumentException("메모는 500자 이내여야 합니다.");
        }
        if (row.getTradingAt() == null) {
            throw new IllegalArgumentException("거래일은 필수입니다.");
        }
        if (row.getAccountId() == null) {
            throw new IllegalArgumentException("계좌 ID는 필수입니다.");
        }
        AccountEntity accountEntity = accountRepository.findById(row.getAccountId())
                                                       .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));
        if (!Objects.equals(accountEntity.getUserId(), userId)) {
            throw new IllegalArgumentException("권한이 없는 계좌입니다.");
        }
        if (row.getCategoryId() == null) {
            throw new IllegalArgumentException("카테고리 ID는 필수입니다.");
        }
        CategoryEntity categoryEntity = categoryRepository.findById(row.getCategoryId())
                                                          .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        if (!Objects.equals(categoryEntity.getUserId(), userId)) {
            throw new IllegalArgumentException("권한이 없는 카테고리입니다.");
        }
        if (CategoryEnum.EXPENSE.equals(categoryEntity.getType())) {
            if (row.getPaymentId() == null) {
                throw new IllegalArgumentException("비용 카테고리는 결제수단이 필요합니다.");
            }
            PaymentEntity paymentEntity = paymentRepository.findById(row.getPaymentId())
                                                           .orElseThrow(() -> new IllegalArgumentException("결제수단을 찾을 수 없습니다."));
            if (!Objects.equals(paymentEntity.getUserId(), userId)) {
                throw new IllegalArgumentException("권한이 없는 결제수단입니다.");
            }
        }
        transactionCategoryPaymentRuleValidator.validateIncomePaymentForbidden(categoryEntity.getType(), row.getPaymentId());
        if (row.getAmount() == null || row.getAmount() <= 0) {
            throw new IllegalArgumentException("금액은 1 이상이어야 합니다.");
        }
    }
}
