package com.moneylog_backend.moneylog.transaction.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.moneylog_backend.global.type.CategoryEnum;

class TransactionCategoryPaymentRuleValidatorTest {
    private TransactionCategoryPaymentRuleValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TransactionCategoryPaymentRuleValidator();
    }

    @Test
    void 수입_카테고리에_결제수단이_있으면_예외를_던진다() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateIncomePaymentForbidden(CategoryEnum.INCOME, 1)
        );
        assertEquals("수입 카테고리에는 결제수단을 지정할 수 없습니다.", exception.getMessage());
    }

    @Test
    void 수입_카테고리에_결제수단이_없으면_통과한다() {
        assertDoesNotThrow(() -> validator.validateIncomePaymentForbidden(CategoryEnum.INCOME, null));
    }

    @Test
    void 지출_카테고리는_결제수단_유무와_관계없이_통과한다() {
        assertDoesNotThrow(() -> validator.validateIncomePaymentForbidden(CategoryEnum.EXPENSE, 1));
        assertDoesNotThrow(() -> validator.validateIncomePaymentForbidden(CategoryEnum.EXPENSE, null));
    }
}
