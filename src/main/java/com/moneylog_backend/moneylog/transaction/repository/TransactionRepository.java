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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TransactionEntity t where t.transactionId = :transactionId")
    Optional<TransactionEntity> findByIdForUpdate (@Param("transactionId") Integer transactionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TransactionEntity> findByInstallmentPlanIdAndInstallmentNoBetweenAndIsSettledFalseOrderByInstallmentNoAsc (
        Integer installmentPlanId,
        Integer startInstallmentNo,
        Integer endInstallmentNo
    );
}
