package com.moneylog_backend.global.type;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentEnum {
    CASH("CASH", "현금"),
    CREDIT_CARD("CREDIT_CARD", "신용카드"),
    CHECK_CARD("CHECK_CARD", "체크카드"),
    BANK("BANK", "은행");

    private final String code;
    private final String label;

    public static PaymentEnum fromCode(String code) {
        return Arrays.stream(values())
                     .filter(type -> type.code.equalsIgnoreCase(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown payment type: " + code));
    }
}
