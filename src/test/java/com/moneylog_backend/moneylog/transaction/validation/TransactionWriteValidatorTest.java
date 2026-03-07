package com.moneylog_backend.moneylog.transaction.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.global.type.PaymentEnum;

class TransactionWriteValidatorTest {
    private TransactionWriteValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TransactionWriteValidator(new TransactionCategoryPaymentRuleValidator());
    }

    @Test
    void 제목이_비어있으면_예외를_던진다() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateBasicFields("  ", null, 1000, LocalDate.now())
        );

        assertEquals("제목은 필수입니다.", exception.getMessage());
    }

    @Test
    void 지출_카테고리에_결제수단이_없으면_예외를_던진다() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validatePaymentPolicy(CategoryEnum.EXPENSE, null)
        );

        assertEquals("지출 카테고리에는 결제수단이 필요합니다.", exception.getMessage());
    }

    @Test
    void 수입_카테고리_할부는_예외를_던진다() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateInstallment(CategoryEnum.INCOME, 2, PaymentEnum.CREDIT_CARD)
        );

        assertEquals("수입 카테고리에는 할부를 적용할 수 없습니다.", exception.getMessage());
    }

    @Test
    void 카드_결제수단이면_지출_할부_검증을_통과한다() {
        assertDoesNotThrow(() -> validator.validateInstallment(CategoryEnum.EXPENSE, 3, PaymentEnum.CREDIT_CARD));
    }
}
