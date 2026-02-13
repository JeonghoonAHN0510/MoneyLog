package com.moneylog_backend.global.type;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountTypeEnum {
    BANK("BANK", "은행"),
    CASH("CASH", "현금"),
    POINT("POINT", "포인트"),
    OTHER("OTHER", "기타");

    private final String code;
    private final String label;

    public static AccountTypeEnum fromCode(String code) {
        return Arrays.stream(values())
                     .filter(type -> type.code.equalsIgnoreCase(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown account type: " + code));
    }
}
