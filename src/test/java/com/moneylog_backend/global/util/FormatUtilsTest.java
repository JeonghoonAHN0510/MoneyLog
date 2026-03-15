package com.moneylog_backend.global.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FormatUtilsTest {
    private final FormatUtils formatUtils = new FormatUtils();

    @Test
    void 전화번호는_구분자가_섞여도_하이픈_형식으로_정규화한다() {
        assertEquals("010-1234-5678", formatUtils.toPhone("010 1234-5678"));
    }

    @Test
    void 열자리_전화번호도_하이픈_형식으로_정규화한다() {
        assertEquals("011-123-4567", formatUtils.toPhone("0111234567"));
    }
}
