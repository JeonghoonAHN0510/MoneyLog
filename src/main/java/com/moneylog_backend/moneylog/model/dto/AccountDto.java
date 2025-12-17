package com.moneylog_backend.moneylog.model.dto;

import com.moneylog_backend.moneylog.model.entity.AccountEntity;
import com.moneylog_backend.moneylog.model.entity.UserEntity;

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
    private String nickname;
    private String bank_name;
    private int balance;
    private String account_number;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public AccountEntity toEntity(UserEntity userEntity){
        return AccountEntity.builder()
                .account_id(this.account_id)
                .userEntity(userEntity)
                .nickname(this.nickname)
                .bank_name(this.bank_name)
                .balance(this.balance)
                .account_number(this.account_number)
                .build();
    } // func end
} // class end