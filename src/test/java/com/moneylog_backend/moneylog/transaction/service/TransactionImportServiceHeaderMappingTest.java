package com.moneylog_backend.moneylog.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.moneylog_backend.moneylog.transaction.importer.HeaderProfileResolver;

class TransactionImportServiceHeaderMappingTest {
    private HeaderProfileResolver headerProfileResolver;

    @BeforeEach
    void setUp() {
        headerProfileResolver = new HeaderProfileResolver();
    }

    @Test
    void 출금_헤더는_일반_금액보다_우선해_debitAmount로_매핑된다() {
        assertEquals("debitAmount", headerProfileResolver.detectHeaderField(normalize("출금금액(원)")));
    }

    @Test
    void 입금_헤더는_일반_금액보다_우선해_creditAmount로_매핑된다() {
        assertEquals("creditAmount", headerProfileResolver.detectHeaderField(normalize("입금금액(원)")));
    }

    @Test
    void 일반_금액_헤더는_amount로_매핑된다() {
        assertEquals("amount", headerProfileResolver.detectHeaderField(normalize("금액")));
    }

    @Test
    void 입금출금_분리_헤더는_amount_오매핑없이_각각_인덱싱된다() {
        Map<String, Integer> headerIndex = headerProfileResolver.resolveHeaderIndex(
            List.of("거래일자", "적요", "출금금액(원)", "입금금액(원)")
        );

        assertEquals(2, headerIndex.get("debitAmount"));
        assertEquals(3, headerIndex.get("creditAmount"));
        assertFalse(headerIndex.containsKey("amount"));
    }

    private String normalize (String raw) {
        String value = Normalizer.normalize(raw.trim().toLowerCase(), Normalizer.Form.NFKC);
        return value.replaceAll("\\s+", "");
    }
}
