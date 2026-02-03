package com.moneylog_backend.moneylog.ledger.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.moneylog.ledger.dto.LedgerDto;

import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ledger")
@Data
@Builder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class LedgerEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_id", columnDefinition = "INT UNSIGNED")
    private Integer ledgerId;
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

    public LedgerDto toDto () {
        return LedgerDto.builder()
                        .ledgerId(this.ledgerId)
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