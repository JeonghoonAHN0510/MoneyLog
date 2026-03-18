package com.moneylog_backend.moneylog.transaction.repository;

import com.moneylog_backend.moneylog.transaction.entity.TransactionEntity;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Integer> {
    int countByInstallmentPlanIdAndIsSettledTrue (Integer installmentPlanId);

    int countByInstallmentPlanId (Integer installmentPlanId);

    TransactionEntity findFirstByInstallmentPlanIdAndIsSettledTrueOrderBySettledAtDesc (Integer installmentPlanId);

    List<TransactionEntity> findByInstallmentPlanIdAndInstallmentNoBetweenAndIsSettledFalseOrderByInstallmentNoAsc (
        Integer installmentPlanId,
        Integer startInstallmentNo,
        Integer endInstallmentNo
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TransactionEntity t WHERE t.transactionId = :transactionId")
    Optional<TransactionEntity> findByIdForUpdate (@Param("transactionId") Integer transactionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t
        FROM TransactionEntity t
        WHERE t.installmentPlanId = :installmentPlanId
          AND t.installmentNo BETWEEN :startInstallmentNo AND :endInstallmentNo
          AND t.isSettled = false
        ORDER BY t.installmentNo ASC
        """)
    List<TransactionEntity> findByInstallmentPlanIdAndInstallmentNoBetweenAndIsSettledFalseOrderByInstallmentNoAscForUpdate (
        @Param("installmentPlanId") Integer installmentPlanId,
        @Param("startInstallmentNo") Integer startInstallmentNo,
        @Param("endInstallmentNo") Integer endInstallmentNo
    );
}
