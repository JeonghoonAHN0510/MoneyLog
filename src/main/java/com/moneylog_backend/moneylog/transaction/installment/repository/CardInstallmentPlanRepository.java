package com.moneylog_backend.moneylog.transaction.installment.repository;

import com.moneylog_backend.moneylog.transaction.installment.entity.CardInstallmentPlanEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardInstallmentPlanRepository extends JpaRepository<CardInstallmentPlanEntity, Integer> {
    List<CardInstallmentPlanEntity> findByIsActiveTrueAndIsCompletedFalseAndFirstTradingAtLessThanEqual (
        LocalDate tradingAt
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM CardInstallmentPlanEntity p WHERE p.installmentPlanId = :installmentPlanId")
    Optional<CardInstallmentPlanEntity> findByIdForUpdate (@Param("installmentPlanId") Integer installmentPlanId);
}
