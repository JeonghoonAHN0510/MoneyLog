package com.moneylog_backend.moneylog.ledger.dto.query;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SelectLedgerByUserIdQuery {
    private int user_id;
    private LocalDate start_date;
    private LocalDate end_date;
}
