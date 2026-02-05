package com.moneylog_backend.moneylog.transaction.dto;

import com.moneylog_backend.moneylog.transaction.entity.TransferEntity;

import java.time.LocalDate;
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
public class TransferDto {
    private Integer transferId;
    private Integer userId;
    private Integer fromAccount;
    private Integer toAccount;
    private Integer amount;
    private LocalDate transferAt;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void setLoginUserId (int userId) {
        this.userId = userId;
    }

    public TransferEntity toEntity (Integer userId) {
        return TransferEntity.builder()
                             .userId(userId)
                             .fromAccount(this.fromAccount)
                             .toAccount(this.toAccount)
                             .amount(this.amount)
                             .transferAt(this.transferAt)
                             .memo(this.memo)
                             .build();
    }
}