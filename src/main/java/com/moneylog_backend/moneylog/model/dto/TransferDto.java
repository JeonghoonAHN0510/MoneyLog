package com.moneylog_backend.moneylog.model.dto;

import com.moneylog_backend.moneylog.model.entity.AccountEntity;
import com.moneylog_backend.moneylog.model.entity.TransferEntity;
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
public class TransferDto {
    private int transfer_id;
    private int user_id;
    private int from_account;
    private int to_account;
    private int amount;
    private LocalDate transfer_at;
    private String memo;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public TransferEntity toEntity(UserEntity userEntity,
                                   AccountEntity fromAccount,
                                   AccountEntity toAccount){
        return TransferEntity.builder()
                .transfer_id(this.transfer_id)
                .userEntity(userEntity)
                .fromAccountEntity(fromAccount)
                .toAccountEntity(toAccount)
                .amount(this.amount)
                .transfer_at(this.transfer_at)
                .memo(this.memo)
                .build();
    } // func end
} // class end