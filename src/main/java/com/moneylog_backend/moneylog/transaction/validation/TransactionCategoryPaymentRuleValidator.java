package com.moneylog_backend.moneylog.transaction.validation;

import org.springframework.stereotype.Component;

import com.moneylog_backend.global.type.CategoryEnum;

@Component
public class TransactionCategoryPaymentRuleValidator {
    public void validateIncomePaymentForbidden (CategoryEnum categoryType, Integer paymentId) {
        if (CategoryEnum.INCOME.equals(categoryType) && paymentId != null) {
            throw new IllegalArgumentException("수입 카테고리에는 결제수단을 지정할 수 없습니다.");
        }
    }
}
