package com.moneylog_backend.moneylog.account.dto.req;

import com.moneylog_backend.global.type.AccountTypeEnum;
import com.moneylog_backend.global.type.ColorEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountReqDto {
    private Integer accountId;
    private Integer bankId;

    @NotBlank(message = "계좌 별명은 필수입니다")
    @Size(max = 30, message = "별명은 30자 이내여야 합니다")
    private String nickname;

    @Min(value = 0, message = "잔액은 0 이상이어야 합니다")
    private Integer balance;

    private String accountNumber;

    @NotNull(message = "계좌 유형은 필수입니다")
    private AccountTypeEnum type;

    private ColorEnum color;

    public AccountEntity toEntity(int userId, String nickname, String regexAccountNumber) {
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
