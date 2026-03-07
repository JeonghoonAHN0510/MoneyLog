package com.moneylog_backend.moneylog.transaction.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;
import com.moneylog_backend.moneylog.payment.repository.PaymentRepository;
import com.moneylog_backend.moneylog.transaction.dto.req.TransactionImportCommitRowDto;
import com.moneylog_backend.moneylog.transaction.validation.TransactionCategoryPaymentRuleValidator;
import com.moneylog_backend.moneylog.transaction.validation.TransactionWriteValidator;

class TransactionImportCommitValidatorTest {
    private AccountRepository accountRepository;
    private CategoryRepository categoryRepository;
    private PaymentRepository paymentRepository;
    private TransactionImportCommitValidator validator;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        validator = new TransactionImportCommitValidator(
            accountRepository,
            categoryRepository,
            paymentRepository,
            new TransactionWriteValidator(new TransactionCategoryPaymentRuleValidator())
        );
    }

    @Test
    void 제목이_null이면_검증에서_차단한다() {
        TransactionImportCommitRowDto row = TransactionImportCommitRowDto.builder()
                                                                         .title(null)
                                                                         .build();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateCommitRow(row, 1)
        );

        assertEquals("제목은 필수입니다.", exception.getMessage());
        verifyNoInteractions(accountRepository, categoryRepository, paymentRepository);
    }

    @Test
    void 제목이_공백이면_검증에서_차단한다() {
        TransactionImportCommitRowDto row = TransactionImportCommitRowDto.builder()
                                                                         .title("   ")
                                                                         .build();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateCommitRow(row, 1)
        );

        assertEquals("제목은 필수입니다.", exception.getMessage());
        verifyNoInteractions(accountRepository, categoryRepository, paymentRepository);
    }

    @Test
    void 제목이_100자를_초과하면_검증에서_차단한다() {
        String longTitle = "a".repeat(101);
        TransactionImportCommitRowDto row = TransactionImportCommitRowDto.builder()
                                                                         .title(longTitle)
                                                                         .build();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateCommitRow(row, 1)
        );

        assertEquals("제목은 100자 이내여야 합니다.", exception.getMessage());
        verifyNoInteractions(accountRepository, categoryRepository, paymentRepository);
    }

    @Test
    void 메모가_500자를_초과하면_검증에서_차단한다() {
        String longMemo = "m".repeat(501);
        TransactionImportCommitRowDto row = TransactionImportCommitRowDto.builder()
                                                                         .title("정상 제목")
                                                                         .memo(longMemo)
                                                                         .build();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateCommitRow(row, 1)
        );

        assertEquals("메모는 500자 이내여야 합니다.", exception.getMessage());
        verifyNoInteractions(accountRepository, categoryRepository, paymentRepository);
    }
}
