package com.moneylog_backend.moneylog.transaction.dto.res;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailySummaryResDto {
    private LocalDate date;
    private Long totalIncome;
    private Long totalExpense;
}
