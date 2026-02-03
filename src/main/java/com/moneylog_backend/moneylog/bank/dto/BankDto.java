package com.moneylog_backend.moneylog.bank.dto;

import com.moneylog_backend.moneylog.bank.entity.BankEntity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankDto {
    private Integer bankId;
    private String name;
    private String code;
    private String logoImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BankEntity toEntity () {
        return BankEntity.builder()
                         .bankId(this.bankId)
                         .name(this.name)
                         .code(this.code)
                         .logoImageUrl(this.logoImageUrl)
                         .build();
    }
}