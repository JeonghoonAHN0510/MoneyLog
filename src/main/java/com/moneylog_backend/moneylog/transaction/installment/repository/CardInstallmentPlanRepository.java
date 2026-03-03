package com.moneylog_backend.moneylog.transaction.installment.repository;

import com.moneylog_backend.moneylog.transaction.installment.entity.CardInstallmentPlanEntity;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CardInstallmentPlanRepository extends JpaRepository<CardInstallmentPlanEntity, Integer> {
    List<CardInstallmentPlanEntity> findByIsActiveTrueAndIsCompletedFalseAndFirstTradingAtLessThanEqual (
        LocalDate tradingAt
    );
}
