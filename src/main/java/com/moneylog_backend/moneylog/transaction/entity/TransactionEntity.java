package com.moneylog_backend.moneylog.transaction.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionResDto;

import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;

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
                             .tradingAt(this.tradingAt)
                             .createdAt(this.getCreatedAt())
                             .updatedAt(this.getUpdatedAt())
                             .build();
    }
}