package com.moneylog_backend.moneylog.transaction.dto.query;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SelectTransactionByUserIdQuery {
    private int userId;
    private LocalDate startDate;
    private LocalDate endDate;
}
