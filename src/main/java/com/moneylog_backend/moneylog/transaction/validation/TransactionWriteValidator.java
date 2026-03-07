package com.moneylog_backend.moneylog.transaction.validation;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.global.type.PaymentEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionWriteValidator {
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_MEMO_LENGTH = 500;
    private static final int MAX_INSTALLMENT_COUNT = 36;

    private final TransactionCategoryPaymentRuleValidator transactionCategoryPaymentRuleValidator;

    public void validateBasicFields(String title, String memo, Integer amount, LocalDate tradingAt) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("제목은 100자 이내여야 합니다.");
        }
        if (memo != null && memo.length() > MAX_MEMO_LENGTH) {
            throw new IllegalArgumentException("메모는 500자 이내여야 합니다.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("금액은 1원 이상이어야 합니다.");
        }
        if (tradingAt == null) {
            throw new IllegalArgumentException("거래일은 필수입니다.");
        }
    }

    public void validateRequiredId(Integer value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "는 필수입니다.");
        }
    }

    public void validatePaymentPolicy(CategoryEnum categoryType, Integer paymentId) {
        if (CategoryEnum.EXPENSE.equals(categoryType)) {
            if (paymentId == null) {
                throw new IllegalArgumentException("지출 카테고리에는 결제수단이 필요합니다.");
            }
            return;
        }

        transactionCategoryPaymentRuleValidator.validateIncomePaymentForbidden(categoryType, paymentId);
    }

    public void validateInstallment(CategoryEnum categoryType, Integer installmentCount, PaymentEnum paymentType) {
        if (!CategoryEnum.EXPENSE.equals(categoryType)) {
            throw new IllegalArgumentException("수입 카테고리에는 할부를 적용할 수 없습니다.");
        }
        if (installmentCount == null || installmentCount < 2) {
            throw new IllegalArgumentException("할부 적용 시 할부 개월 수가 필요합니다.");
        }
        if (installmentCount > MAX_INSTALLMENT_COUNT) {
            throw new IllegalArgumentException("할부는 최대 " + MAX_INSTALLMENT_COUNT + "개월까지 지원됩니다.");
        }
        if (!PaymentEnum.CREDIT_CARD.equals(paymentType) && !PaymentEnum.CHECK_CARD.equals(paymentType)) {
            throw new IllegalArgumentException("할부는 카드 결제수단만 가능합니다.");
        }
    }
}
