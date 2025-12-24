package com.moneylog_backend.moneylog.account.dto;

import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.global.common.entity.BankEntity;
import com.moneylog_backend.moneylog.user.entity.UserEntity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto {
    private int account_id;
    private int user_id;
    private int bank_id;
    private String nickname;
    private int balance;
    private String account_number;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public AccountEntity toEntity(UserEntity userEntity,
                                  BankEntity bankEntity){
        return AccountEntity.builder()
                .account_id(this.account_id)
                .userEntity(userEntity)
                .bankEntity(bankEntity)
                .nickname(this.nickname)
                .balance(this.balance)
                .account_number(this.account_number)
                .build();
    } // func end
} // class end