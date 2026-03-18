package com.moneylog_backend.moneylog.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.lang.reflect.Method;
import java.util.List;

import com.moneylog_backend.global.type.AccountTypeEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.service.AccountLockHelper;
import com.moneylog_backend.moneylog.transaction.entity.TransactionEntity;
import com.moneylog_backend.moneylog.transaction.installment.entity.CardInstallmentPlanEntity;
import com.moneylog_backend.moneylog.transaction.installment.repository.CardInstallmentPlanRepository;
import com.moneylog_backend.moneylog.transaction.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class TransactionSettlementServiceTest {
    private TransactionRepository transactionRepository;
    private CardInstallmentPlanRepository installmentPlanRepository;
    private AccountLockHelper accountLockHelper;
    private TransactionSettlementService transactionSettlementService;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        installmentPlanRepository = mock(CardInstallmentPlanRepository.class);
        accountLockHelper = mock(AccountLockHelper.class);
        transactionSettlementService = new TransactionSettlementService(
            transactionRepository,
            installmentPlanRepository,
            accountLockHelper
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

    @Test
    void 할부_정산은_plan_transaction_account_순서로_잠근다() {
        CardInstallmentPlanEntity plan = CardInstallmentPlanEntity.builder()
                                                                  .installmentPlanId(11)
                                                                  .userId(7)
                                                                  .accountId(30)
                                                                  .installmentCount(3)
                                                                  .firstTradingAt(LocalDate.now().minusMonths(1))
                                                                  .isActive(true)
                                                                  .isCompleted(false)
                                                                  .settledCount(0)
                                                                  .build();
        TransactionEntity dueTransaction = TransactionEntity.builder()
                                                            .transactionId(1001)
                                                            .installmentPlanId(11)
                                                            .installmentNo(1)
                                                            .amount(500)
                                                            .isSettled(false)
                                                            .build();
        AccountEntity accountEntity = AccountEntity.builder()
                                                   .accountId(30)
                                                   .userId(7)
                                                   .balance(2000)
                                                   .type(AccountTypeEnum.BANK)
                                                   .nickname("정산")
                                                   .build();

        when(installmentPlanRepository.findByIdForUpdate(11)).thenReturn(java.util.Optional.of(plan));
        when(transactionRepository.countByInstallmentPlanId(11)).thenReturn(3);
        when(transactionRepository.countByInstallmentPlanIdAndIsSettledTrue(11)).thenReturn(0);
        when(transactionRepository.findFirstByInstallmentPlanIdAndIsSettledTrueOrderBySettledAtDesc(11)).thenReturn(null);
        when(transactionRepository.findByInstallmentPlanIdAndInstallmentNoBetweenAndIsSettledFalseOrderByInstallmentNoAscForUpdate(
            11,
            1,
            2
        )).thenReturn(List.of(dueTransaction));
        when(accountLockHelper.lockOwnedAccount(30, 7)).thenReturn(accountEntity);

        transactionSettlementService.settleInstallmentPlan(11);

        InOrder inOrder = inOrder(installmentPlanRepository, transactionRepository, accountLockHelper);
        inOrder.verify(installmentPlanRepository).findByIdForUpdate(11);
        inOrder.verify(transactionRepository)
               .findByInstallmentPlanIdAndInstallmentNoBetweenAndIsSettledFalseOrderByInstallmentNoAscForUpdate(11, 1, 2);
        inOrder.verify(accountLockHelper).lockOwnedAccount(30, 7);

        assertTrue(dueTransaction.getIsSettled());
        assertEquals(1500, accountEntity.getBalance());
    }
}
