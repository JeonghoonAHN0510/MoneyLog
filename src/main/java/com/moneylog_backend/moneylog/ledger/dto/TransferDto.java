package com.moneylog_backend.moneylog.ledger.dto;

import com.moneylog_backend.moneylog.ledger.entity.TransferEntity;

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

    public TransferEntity toEntity () {
        return TransferEntity.builder()
                             .transfer_id(this.transfer_id)
                             .user_id(this.user_id)
                             .from_account(this.from_account)
                             .to_account(this.to_account)
                             .amount(this.amount)
                             .transfer_at(this.transfer_at)
                             .memo(this.memo)
                             .build();
    }
}