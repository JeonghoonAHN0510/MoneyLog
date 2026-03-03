package com.moneylog_backend.moneylog.transaction.service;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.global.util.OwnershipValidator;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.transaction.entity.TransactionEntity;
import com.moneylog_backend.moneylog.transaction.installment.entity.CardInstallmentPlanEntity;
import com.moneylog_backend.moneylog.transaction.installment.repository.CardInstallmentPlanRepository;
import com.moneylog_backend.moneylog.transaction.repository.TransactionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import com.moneylog_backend.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionSettlementService {
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    private final TransactionRepository transactionRepository;
    private final CardInstallmentPlanRepository installmentPlanRepository;
    private final AccountRepository accountRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void settleInstallmentPlan (Integer installmentPlanId) {
        CardInstallmentPlanEntity plan = installmentPlanRepository.findById(installmentPlanId)
                                                                .orElseThrow(
                                                                    () -> new IllegalArgumentException("할부 계획을 찾을 수 없습니다."));

        resyncInstallmentPlanProgress(plan);

        int currentSettledCount = safe(plan.getSettledCount());
        int plannedInstallmentCount = safe(plan.getInstallmentCount());
        int targetSettledCount = calculateDueInstallmentCount(plan.getFirstTradingAt(),
                                                             plannedInstallmentCount,
                                                             nowDateKst());

        if (targetSettledCount <= currentSettledCount) {
            return;
        }

        List<TransactionEntity> dueTransactions = getDueInstallmentTransactions(plan.getInstallmentPlanId(),
                                                                              currentSettledCount,
                                                                              targetSettledCount);
        if (dueTransactions.isEmpty()) {
            return;
        }

        AccountEntity accountEntity = getAccountByIdAndValidateOwnership(plan.getAccountId(), plan.getUserId());

        for (TransactionEntity transactionEntity : dueTransactions) {
            try {
                settleTransaction(transactionEntity, accountEntity);
                log.info("할부 자동 정산 완료: transactionId={}, installmentPlanId={}, installmentNo={}",
                         transactionEntity.getTransactionId(), plan.getInstallmentPlanId(), transactionEntity.getInstallmentNo());
            } catch (Exception e) {
                log.error("할부 항목 정산 실패: transactionId={}, installmentPlanId={}, installmentNo={}",
                          transactionEntity.getTransactionId(), plan.getInstallmentPlanId(), transactionEntity.getInstallmentNo(), e);
            }
        }

        resyncInstallmentPlanProgress(plan);
    }

    private void settleTransaction (TransactionEntity transactionEntity, AccountEntity accountEntity) {
        if (Boolean.TRUE.equals(transactionEntity.getIsSettled())) {
            return;
        }

        updateAccountBalance(accountEntity, CategoryEnum.EXPENSE, transactionEntity.getAmount(), false);
        transactionEntity.markAsSettled(nowDateTimeKst());
    }

    private void updateAccountBalance (AccountEntity account, CategoryEnum type, int amount, boolean isRevert) {
        boolean isExpense = CategoryEnum.EXPENSE.equals(type);

        if (isRevert) {
            if (isExpense) {
                account.deposit(amount);
            }
        } else {
            if (isExpense) {
                account.withdraw(amount);
            }
        }
    }

    private int calculateDueInstallmentCount (LocalDate firstTradingAt, int totalCount, LocalDate today) {
        if (firstTradingAt == null || totalCount <= 0 || today.isBefore(firstTradingAt)) {
            return 0;
        }

        int dueCount = 0;
        for (int i = 0; i < totalCount; i++) {
            LocalDate installmentTradingAt = firstTradingAt.plusMonths(i);
            if (installmentTradingAt.isAfter(today)) {
                break;
            }
            dueCount++;
        }

        return dueCount;
    }

    private int safe (Integer value) {
        return value == null ? 0 : value;
    }

    private LocalDate nowDateKst () {
        return LocalDate.now(KST_ZONE);
    }

    private LocalDateTime nowDateTimeKst () {
        return LocalDateTime.now(KST_ZONE);
    }

    private int getActualInstallmentCount (Integer installmentPlanId) {
        return Math.max(transactionRepository.countByInstallmentPlanId(installmentPlanId), 0);
    }

    private int getActualSettledInstallmentCount (Integer installmentPlanId) {
        return Math.max(transactionRepository.countByInstallmentPlanIdAndIsSettledTrue(installmentPlanId), 0);
    }

    private LocalDateTime getLatestSettledAtByPlan (Integer installmentPlanId) {
        TransactionEntity latestSettledTransaction = transactionRepository
            .findFirstByInstallmentPlanIdAndIsSettledTrueOrderBySettledAtDesc(installmentPlanId);
        if (latestSettledTransaction == null) {
            return null;
        }

        return latestSettledTransaction.getSettledAt();
    }

    public void resyncInstallmentPlanProgress (CardInstallmentPlanEntity plan) {
        Integer planId = plan.getInstallmentPlanId();
        int activeInstallmentCount = getActualInstallmentCount(planId);
        int settledCount = getActualSettledInstallmentCount(planId);
        LocalDateTime latestSettledAt = getLatestSettledAtByPlan(planId);

        plan.resyncProgress(activeInstallmentCount, settledCount, latestSettledAt);
    }

    private List<TransactionEntity> getDueInstallmentTransactions (Integer installmentPlanId,
                                                                  Integer currentSettledCount,
                                                                  Integer targetSettledCount) {
        if (targetSettledCount <= currentSettledCount) {
            return List.of();
        }

        int startNo = currentSettledCount + 1;
        return transactionRepository.findByInstallmentPlanIdAndInstallmentNoBetweenAndIsSettledFalseOrderByInstallmentNoAsc(
            installmentPlanId, startNo, targetSettledCount);
    }

    private AccountEntity getAccountByIdAndValidateOwnership (Integer accountId, Integer userId) {
        AccountEntity accountEntity = accountRepository.findById(accountId)
                                                     .orElseThrow(() -> new ResourceNotFoundException("계좌를 찾을 수 없습니다."));
        OwnershipValidator.validateOwner(accountEntity.getUserId(), userId, "본인의 계좌가 아닙니다.");

        return accountEntity;
    }
}
