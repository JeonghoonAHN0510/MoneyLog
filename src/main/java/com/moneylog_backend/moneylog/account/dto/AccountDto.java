package com.moneylog_backend.moneylog.account.dto;

import com.moneylog_backend.global.type.ColorEnum;
import com.moneylog_backend.global.type.AccountTypeEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;

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
    private Integer accountId;
    private Integer userId;
    private Integer bankId;
    private String nickname;
    private Integer balance;
    private String accountNumber;
    private AccountTypeEnum type;
    private ColorEnum color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String bankName;

    public AccountEntity toEntity () {
        return AccountEntity.builder()
                            .accountId(this.accountId)
                            .userId(this.userId)
                            .bankId(this.bankId)
                            .nickname(this.nickname)
                            .balance(this.balance)
                            .accountNumber(this.accountNumber)
                            .color(this.color)
                            .type(this.type)
                            .build();
    }
}