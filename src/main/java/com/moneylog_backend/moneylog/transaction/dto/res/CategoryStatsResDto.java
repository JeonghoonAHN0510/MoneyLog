package com.moneylog_backend.moneylog.transaction.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryStatsResDto {
    private String categoryName;
    private Long totalAmount;
    private Double ratio;
}
