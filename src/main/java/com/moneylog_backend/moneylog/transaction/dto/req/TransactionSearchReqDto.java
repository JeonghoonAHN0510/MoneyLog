package com.moneylog_backend.moneylog.transaction.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionSearchReqDto {
    private String startDate; // yyyy-MM-dd
    private String endDate;
    private String keyword;
    private String categoryId; // Optional: 특정 카테고리만
    private String categoryType; // INCOME / EXPENSE
    private Long minAmount;
    private Long maxAmount;
    private String paymentId;
    private Integer userId; // Service에서 주입
}
