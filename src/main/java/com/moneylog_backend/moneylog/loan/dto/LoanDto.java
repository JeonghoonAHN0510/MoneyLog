package com.moneylog_backend.moneylog.loan.dto;

import com.moneylog_backend.global.common.entity.BankEntity;
import com.moneylog_backend.moneylog.loan.entity.LoanEntity;

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
public class LoanDto {
    private int loan_id;
    private int bank_id;
    private int amount;
    private double interest_rate;
    private LocalDate terminated_at;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public LoanEntity toEntity(BankEntity bankEntity){
        return LoanEntity.builder()
                .loan_id(this.loan_id)
                .bankEntity(bankEntity)
                .amount(this.amount)
                .interest_rate(this.interest_rate)
                .terminated_at(this.terminated_at)
                .build();
    } // func end
} // class end