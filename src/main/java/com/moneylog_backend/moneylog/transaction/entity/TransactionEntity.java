package com.moneylog_backend.moneylog.transaction.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionResDto;

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
@Table(name = "transaction")
@Getter
@SuperBuilder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id", columnDefinition = "INT UNSIGNED")
    private Integer transactionId;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer userId;
    @Column(name = "category_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer categoryId;
    @Column(name = "payment_id", columnDefinition = "INT UNSIGNED")
    private Integer paymentId;
    @Column(name = "account_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer accountId;
    @Column(name = "fixed_id", columnDefinition = "INT UNSIGNED")
    private Integer fixedId;
    @Column(columnDefinition = "VARCHAR(100) NOT NULL")
    private String title;
    @Column(columnDefinition = "INT NOT NULL")
    private Integer amount;
    @Column(columnDefinition = "TEXT")
    private String memo;
    @Column(name = "trading_at", columnDefinition = "DATE NOT NULL")
    private LocalDate tradingAt;
    @Column(name = "installment_plan_id", columnDefinition = "INT UNSIGNED")
    private Integer installmentPlanId;
    @Column(name = "installment_no", columnDefinition = "INT UNSIGNED")
    private Integer installmentNo;
    @Column(name = "installment_total_count", columnDefinition = "INT UNSIGNED")
    private Integer installmentTotalCount;
    @Column(name = "is_installment", columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private Boolean isInstallment;
    @Column(name = "is_settled", columnDefinition = "TINYINT(1) NOT NULL DEFAULT 1")
    private Boolean isSettled;
    @Column(name = "is_interest_free", columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private Boolean isInterestFree;
    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    public void update(Integer categoryId, Integer paymentId, Integer accountId,
                       String title, Integer amount, String memo, LocalDate tradingAt) {
        this.categoryId = categoryId;
        this.amount = amount;
        this.accountId = accountId;

        if (paymentId != null) {
            this.paymentId = paymentId;
        }
        if (title != null) {
            this.title = title;
        }
        if (memo != null) {
            this.memo = memo;
        }
        if (tradingAt != null) {
            this.tradingAt = tradingAt;
        }
    }

    public void markAsSettled (LocalDateTime settledAt) {
        this.isSettled = true;
        this.settledAt = settledAt;
    }

    public void initializeDefaultSingleSettlementState (LocalDateTime settledAt) {
        this.isInstallment = false;
        this.isInterestFree = false;
        this.isSettled = true;
        this.settledAt = settledAt;
    }

    public TransactionResDto toDto() {
        return TransactionResDto.builder()
                             .transactionId(this.transactionId)
                             .userId(this.userId)
                             .categoryId(this.categoryId)
                             .paymentId(this.paymentId)
                             .accountId(this.accountId)
                             .fixedId(this.fixedId)
                             .title(this.title)
                             .amount(this.amount)
                             .memo(this.memo)
                             .installmentPlanId(this.installmentPlanId)
                             .installmentNo(this.installmentNo)
                             .installmentTotalCount(this.installmentTotalCount)
                             .isInstallment(this.isInstallment)
                             .isInterestFree(this.isInterestFree)
                             .isSettled(this.isSettled)
                             .settledAt(this.settledAt)
                             .tradingAt(this.tradingAt)
                             .createdAt(this.getCreatedAt())
                             .updatedAt(this.getUpdatedAt())
                             .build();
    }
}
