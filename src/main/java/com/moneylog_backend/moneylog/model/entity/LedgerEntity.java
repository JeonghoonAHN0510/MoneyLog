package com.moneylog_backend.moneylog.model.entity;

import com.moneylog_backend.moneylog.model.dto.LedgerDto;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
@AllArgsConstructor
@NoArgsConstructor
public class LedgerEntity extends BaseTime{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private int ledger_id;
    @Column(columnDefinition = "VARCHAR(100) NOT NULL")
    private String title;
    @Column(columnDefinition = "INT NOT NULL")
    private int amount;
    @Column(columnDefinition = "TEXT")
    private String memo;
    @Column(columnDefinition = "DATE NOT NULL")
    private LocalDate trading_at;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CategoryEntity categoryEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PaymentEntity paymentEntity;

    public LedgerDto toDto(){
        return LedgerDto.builder()
                .ledger_id(this.ledger_id)
                .title(this.title)
                .amount(this.amount)
                .memo(this.memo)
                .trading_at(this.trading_at)
                .user_id(this.userEntity != null ? this.userEntity.getUser_id() : 0)
                .category_id(this.categoryEntity != null ? this.categoryEntity.getCategory_id() : 0)
                .payment_id(this.paymentEntity != null ? this.paymentEntity.getPayment_id() : 0)
                .created_at(this.getCreated_at())
                .updated_at(this.getUpdated_at())
                .build();
    } // func end
} // class end