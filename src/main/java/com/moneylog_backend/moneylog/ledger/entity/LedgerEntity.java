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
    @Column(columnDefinition = "INT UNSIGNED")
    private int ledger_id;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private int user_id;
    @Column(name = "category_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private int category_id;
    @Column(name = "payment_id", columnDefinition = "INT UNSIGNED")
    private int payment_id;
    @Column(name = "account_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private int account_id;
    @Column(name = "fixed_id", columnDefinition = "INT UNSIGNED")
    private int fixed_id;
    @Column(columnDefinition = "VARCHAR(100) NOT NULL")
    private String title;
    @Column(columnDefinition = "INT NOT NULL")
    private int amount;
    @Column(columnDefinition = "TEXT")
    private String memo;
    @Column(columnDefinition = "DATE NOT NULL")
    private LocalDate trading_at;

    public LedgerDto toDto () {
        return LedgerDto.builder()
                        .ledger_id(this.ledger_id)
                        .user_id(this.user_id)
                        .category_id(this.category_id)
                        .payment_id(this.payment_id)
                        .account_id(this.account_id)
                        .fixed_id(this.fixed_id)
                        .title(this.title)
                        .amount(this.amount)
                        .memo(this.memo)
                        .trading_at(this.trading_at)
                        .created_at(this.getCreated_at())
                        .updated_at(this.getUpdated_at())
                        .build();
    } // func end
} // class end