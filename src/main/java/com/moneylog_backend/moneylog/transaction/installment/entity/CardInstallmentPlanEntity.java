package com.moneylog_backend.moneylog.transaction.installment.entity;

import com.moneylog_backend.global.common.BaseTime;

import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "card_installment_plan")
@Getter
@SuperBuilder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardInstallmentPlanEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "installment_plan_id", columnDefinition = "INT UNSIGNED")
    private Integer installmentPlanId;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer userId;
    @Column(name = "category_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer categoryId;
    @Column(name = "payment_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer paymentId;
    @Column(name = "account_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer accountId;
    @Column(columnDefinition = "VARCHAR(100) NOT NULL")
    private String title;
    @Column(columnDefinition = "TEXT")
    private String memo;
    @Column(columnDefinition = "INT NOT NULL")
    private Integer totalAmount;
    @Column(name = "installment_count", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer installmentCount;
    @Column(name = "is_interest_free", columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private Boolean isInterestFree;
    @Column(name = "first_trading_at", columnDefinition = "DATE NOT NULL")
    private LocalDate firstTradingAt;
    @Column(name = "is_active", columnDefinition = "TINYINT(1) NOT NULL DEFAULT 1")
    private Boolean isActive;
    @Column(name = "settled_count", columnDefinition = "INT UNSIGNED NOT NULL DEFAULT 0")
    private Integer settledCount;
    @Column(name = "is_completed", columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private Boolean isCompleted;
    @Column(name = "last_settled_at")
    private LocalDateTime lastSettledAt;

    public void initializeProgress (Integer settledCount, LocalDateTime settledAt) {
        this.settledCount = settledCount;
        this.lastSettledAt = settledAt;
        this.isActive = Boolean.TRUE;
        this.isCompleted = settledCount != null && settledCount >= this.installmentCount;
    }

    public void markSettled (int additionalSettledCount, LocalDateTime settledAt) {
        this.settledCount = (this.settledCount == null ? 0 : this.settledCount) + additionalSettledCount;
        this.lastSettledAt = settledAt;
        if (this.settledCount >= this.installmentCount) {
            this.isCompleted = true;
            this.isActive = false;
            this.settledCount = this.installmentCount;
        }
    }

    public void deactivate () {
        this.isActive = false;
        this.isCompleted = true;
    }

    public void resyncProgress (Integer activeInstallmentCount, Integer settledCount, LocalDateTime lastSettledAt) {
        int totalInstallments = activeInstallmentCount == null ? 0 : Math.max(activeInstallmentCount, 0);
        int safeSettledCount = settledCount == null ? 0 : Math.max(settledCount, 0);

        this.settledCount = Math.min(safeSettledCount, totalInstallments);
        this.lastSettledAt = lastSettledAt;
        this.isCompleted = totalInstallments <= 0 || this.settledCount >= totalInstallments;
        this.isActive = !this.isCompleted;

        if (totalInstallments <= 0) {
            this.settledCount = 0;
        }
    }
}
