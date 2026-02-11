package com.moneylog_backend.moneylog.transaction.dto.res;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResDto {
    private Long totalIncome;
    private Long totalExpense;
    private Long totalBalance;
    private List<CategoryStatsResDto> categoryStats;
}
