package com.moneylog_backend.moneylog.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;
import com.moneylog_backend.moneylog.payment.repository.PaymentRepository;

class TransactionImportServiceHeaderMappingTest {
    private TransactionImportService transactionImportService;

    @BeforeEach
    void setUp() {
        transactionImportService = new TransactionImportService(
            mock(AccountRepository.class),
            mock(CategoryRepository.class),
            mock(PaymentRepository.class),
            mock(TransactionService.class)
        );
    }

    @Test
    void 출금_헤더는_일반_금액보다_우선해_debitAmount로_매핑된다() throws Exception {
        assertEquals("debitAmount", invokeDetectHeaderField("출금금액(원)"));
    }

    @Test
    void 입금_헤더는_일반_금액보다_우선해_creditAmount로_매핑된다() throws Exception {
        assertEquals("creditAmount", invokeDetectHeaderField("입금금액(원)"));
    }

    @Test
    void 일반_금액_헤더는_amount로_매핑된다() throws Exception {
        assertEquals("amount", invokeDetectHeaderField("금액"));
    }

    @Test
    void 입금출금_분리_헤더는_amount_오매핑없이_각각_인덱싱된다() throws Exception {
        Map<String, Integer> headerIndex = invokeResolveHeaderIndex(
            List.of("거래일자", "적요", "출금금액(원)", "입금금액(원)")
        );

        assertEquals(2, headerIndex.get("debitAmount"));
        assertEquals(3, headerIndex.get("creditAmount"));
        assertFalse(headerIndex.containsKey("amount"));
    }

    private String invokeDetectHeaderField (String token) throws Exception {
        Method method = TransactionImportService.class.getDeclaredMethod("detectHeaderField", String.class);
        method.setAccessible(true);
        return (String) method.invoke(transactionImportService, normalize(token));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> invokeResolveHeaderIndex (List<String> headerRow) throws Exception {
        Method method = TransactionImportService.class.getDeclaredMethod("resolveHeaderIndex", List.class);
        method.setAccessible(true);
        return (Map<String, Integer>) method.invoke(transactionImportService, headerRow);
    }

    private String normalize (String raw) {
        String value = Normalizer.normalize(raw.trim().toLowerCase(), Normalizer.Form.NFKC);
        return value.replaceAll("\\s+", "");
    }
}
