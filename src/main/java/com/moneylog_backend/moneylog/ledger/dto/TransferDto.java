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
    private Integer transferId;
    private Integer userId;
    private Integer fromAccount;
    private Integer toAccount;
    private Integer amount;
    private LocalDate transferAt;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TransferEntity toEntity () {
        return TransferEntity.builder()
                             .transferId(this.transferId)
                             .userId(this.userId)
                             .fromAccount(this.fromAccount)
                             .toAccount(this.toAccount)
                             .amount(this.amount)
                             .transferAt(this.transferAt)
                             .memo(this.memo)
                             .build();
    }
}