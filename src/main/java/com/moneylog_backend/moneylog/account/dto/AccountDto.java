package com.moneylog_backend.moneylog.account.dto;

import com.moneylog_backend.global.type.ColorEnum;
import com.moneylog_backend.global.type.AccountTypeEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;

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
public class AccountDto {
    private Integer account_id;
    private Integer user_id;
    private Integer bank_id;
    private String nickname;
    private Integer balance;
    private String account_number;
    private AccountTypeEnum type;
    private ColorEnum color;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;


    public AccountEntity toEntity () {
        return AccountEntity.builder()
                            .account_id(this.account_id)
                            .user_id(this.user_id)
                            .bank_id(this.bank_id)
                            .nickname(this.nickname)
                            .balance(this.balance)
                            .account_number(this.account_number)
                            .color(this.color)
                            .type(this.type)
                            .build();
    }
}