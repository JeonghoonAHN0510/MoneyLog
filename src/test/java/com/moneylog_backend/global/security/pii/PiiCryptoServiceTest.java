package com.moneylog_backend.global.security.pii;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PiiCryptoServiceTest {
    @Test
    void 암호화된_문자열은_복호화시_원문으로_돌아온다() {
        PiiCryptoService piiCryptoService = new PiiCryptoService("enc-key", "hash-key");

        String encrypted = piiCryptoService.encrypt("tester@moneylog.com");

        assertTrue(encrypted.startsWith("ENCv1:"));
        assertNotEquals("tester@moneylog.com", encrypted);
        assertEquals("tester@moneylog.com", piiCryptoService.decrypt(encrypted));
    }

    @Test
    void 평문_legacy_값은_복호화시_그대로_통과한다() {
        PiiCryptoService piiCryptoService = new PiiCryptoService("enc-key", "hash-key");

        assertEquals("legacy@example.com", piiCryptoService.decrypt("legacy@example.com"));
    }

    @Test
    void 이메일_정규화와_해시는_대소문자와_공백을_통일한다() {
        PiiCryptoService piiCryptoService = new PiiCryptoService("enc-key", "hash-key");

        String normalizedA = piiCryptoService.normalizeEmail(" Tester@MoneyLog.com ");
        String normalizedB = piiCryptoService.normalizeEmail("tester@moneylog.com");

        assertEquals("tester@moneylog.com", normalizedA);
        assertEquals(normalizedA, normalizedB);
        assertEquals(piiCryptoService.hashEmail(normalizedA), piiCryptoService.hashEmail(normalizedB));
    }

    @Test
    void 키가_없으면_암호화_사용_시점에_실패한다() {
        PiiCryptoService piiCryptoService = new PiiCryptoService("", "");

        assertThrows(IllegalStateException.class, () -> piiCryptoService.encrypt("value"));
        assertThrows(IllegalStateException.class, () -> piiCryptoService.hashEmail("tester@moneylog.com"));
    }
}
