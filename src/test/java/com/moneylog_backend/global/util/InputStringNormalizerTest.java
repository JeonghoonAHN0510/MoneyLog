package com.moneylog_backend.global.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class InputStringNormalizerTest {
    @Test
    void trimToNull은_공백만_있는_값을_null로_바꾼다() {
        assertNull(InputStringNormalizer.trimToNull("   "));
    }

    @Test
    void trimNullable는_null은_유지하고_문자열은_trim한다() {
        assertNull(InputStringNormalizer.trimNullable(null));
        assertEquals("메모", InputStringNormalizer.trimNullable("  메모  "));
    }

    @Test
    void digitsOnly는_숫자_외_문자를_제거한다() {
        assertEquals("01012345678", InputStringNormalizer.digitsOnly("010-1234 5678"));
    }
}
