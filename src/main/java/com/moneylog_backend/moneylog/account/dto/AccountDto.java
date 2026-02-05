package com.moneylog_backend.moneylog.account.dto;

import com.moneylog_backend.global.type.ColorEnum;
import com.moneylog_backend.global.type.AccountTypeEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public AccountEntity toEntity (int userId, String nickname, String regexAccountNumber) {
        return AccountEntity.builder()
                            .userId(userId)
                            .bankId(this.bankId)
                            .nickname(nickname)
                            .balance(this.balance)
                            .accountNumber(regexAccountNumber)
                            .color(this.color)
                            .type(this.type)
                            .build();
    }
}