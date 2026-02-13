package com.moneylog_backend.global.type;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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

    private static final Map<String, PaymentEnum> CODE_MAP = Arrays.stream(values())
                                                                    .collect(Collectors.toMap(
                                                                        type -> type.code.toUpperCase(Locale.ROOT),
                                                                        type -> type));

    public static PaymentEnum fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Unknown payment type: null");
        }
        PaymentEnum result = CODE_MAP.get(code.toUpperCase(Locale.ROOT));
        if (result == null) {
            throw new IllegalArgumentException("Unknown payment type: " + code);
        }
        return result;
    }
}
