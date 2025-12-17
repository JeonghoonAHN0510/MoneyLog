package com.moneylog_backend.moneylog.model.dto;

import com.moneylog_backend.moneylog.model.entity.CategoryEntity;
import com.moneylog_backend.moneylog.model.entity.LedgerEntity;
import com.moneylog_backend.moneylog.model.entity.PaymentEntity;
import com.moneylog_backend.moneylog.model.entity.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LedgerDto {
    private int ledger_id;
    private int user_id;
    private int category_id;
    private int payment_id;
    private int account_id;
    private String title;
    private int amount;
    private String memo;
    private LocalDate trading_at;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public LedgerEntity toEntity(UserEntity userEntity,
                                 CategoryEntity categoryEntity,
                                 PaymentEntity paymentEntity){
        return LedgerEntity.builder()
                .ledger_id(this.ledger_id)
                .userEntity(userEntity)
                .categoryEntity(categoryEntity)
                .paymentEntity(paymentEntity)
                .title(this.title)
                .amount(this.amount)
                .memo(this.memo)
                .trading_at(this.trading_at)
                .build();
    } // func end
} // class end