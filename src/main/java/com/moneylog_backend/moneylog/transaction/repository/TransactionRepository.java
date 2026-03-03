package com.moneylog_backend.moneylog.transaction.repository;

import com.moneylog_backend.moneylog.transaction.entity.TransactionEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Integer> {
    int countByInstallmentPlanIdAndIsSettledTrue (Integer installmentPlanId);

    int countByInstallmentPlanId (Integer installmentPlanId);

    TransactionEntity findFirstByInstallmentPlanIdAndIsSettledTrueOrderBySettledAtDesc (Integer installmentPlanId);

    List<TransactionEntity> findByInstallmentPlanIdAndInstallmentNoBetweenAndIsSettledFalseOrderByInstallmentNoAsc (
        Integer installmentPlanId,
        Integer startInstallmentNo,
        Integer endInstallmentNo
    );
}
