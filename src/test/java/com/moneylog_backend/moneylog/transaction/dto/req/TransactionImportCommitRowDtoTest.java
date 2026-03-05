package com.moneylog_backend.moneylog.transaction.dto.req;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class TransactionImportCommitRowDtoTest {

    @Test
    void 업로드_커밋_변환은_항상_일시불로_고정된다() {
        TransactionImportCommitRowDto row = TransactionImportCommitRowDto.builder()
            .rowIndex(1)
            .tradingAt(LocalDate.of(2026, 3, 4))
            .title("카드결제")
            .amount(120000)
            .memo("업로드")
            .accountId(30001)
            .categoryId(10001)
            .paymentId(20001)
            .build();

        TransactionReqDto req = row.toTransactionReqDto();

        assertNull(req.getInstallmentCount());
        assertEquals(Boolean.FALSE, req.getIsInterestFree());
        assertFalse(req.isInstallment());
    }
}
