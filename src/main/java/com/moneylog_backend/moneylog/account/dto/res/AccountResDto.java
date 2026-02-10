package com.moneylog_backend.moneylog.account.dto.res;

import com.moneylog_backend.global.type.AccountTypeEnum;
import com.moneylog_backend.global.type.ColorEnum;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AccountResDto {
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
}
