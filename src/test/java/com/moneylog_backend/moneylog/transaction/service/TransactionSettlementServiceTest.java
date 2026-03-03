package com.moneylog_backend.moneylog.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.lang.reflect.Method;

import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.transaction.installment.repository.CardInstallmentPlanRepository;
import com.moneylog_backend.moneylog.transaction.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionSettlementServiceTest {
    private TransactionSettlementService transactionSettlementService;

    @BeforeEach
    void setUp() {
        transactionSettlementService = new TransactionSettlementService(
            mock(TransactionRepository.class),
            mock(CardInstallmentPlanRepository.class),
            mock(AccountRepository.class)
        );
    }

    @Test
    void 월말_시작일의_다음달_말일까지는_둘째_할부가_미뤄지지_않는다() throws Exception {
        Method method = TransactionSettlementService.class.getDeclaredMethod("calculateDueInstallmentCount",
                                                                 LocalDate.class,
                                                                 int.class,
                                                                 LocalDate.class);
        method.setAccessible(true);
        int result = (int) method.invoke(transactionSettlementService,
                                         LocalDate.of(2024, 1, 31),
                                         4,
                                         LocalDate.of(2024, 2, 29));
        assertEquals(2, result);
    }

    @Test
    void 월말_시작일이고_현재일이_첫달보다_앞이면_0이_반환된다() throws Exception {
        Method method = TransactionSettlementService.class.getDeclaredMethod("calculateDueInstallmentCount",
                                                                 LocalDate.class,
                                                                 int.class,
                                                                 LocalDate.class);
        method.setAccessible(true);
        int result = (int) method.invoke(transactionSettlementService,
                                         LocalDate.of(2024, 1, 31),
                                         4,
                                         LocalDate.of(2024, 1, 30));
        assertEquals(0, result);
    }

    @Test
    void 전체_회차가_지난_경우_총_회차_수가_반환된다() throws Exception {
        Method method = TransactionSettlementService.class.getDeclaredMethod("calculateDueInstallmentCount",
                                                                 LocalDate.class,
                                                                 int.class,
                                                                 LocalDate.class);
        method.setAccessible(true);
        int result = (int) method.invoke(transactionSettlementService,
                                         LocalDate.of(2024, 1, 31),
                                         4,
                                         LocalDate.of(2024, 12, 31));
        assertEquals(4, result);
    }
}
