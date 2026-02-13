package com.moneylog_backend.global.type;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusEnum {
    ACTIVE("ACTIVE", "활성"),
    DORMANT("DORMANT", "휴면"),
    WITHDRAWN("WITHDRAWN", "탈퇴");

    private final String code;
    private final String label;

    public static StatusEnum fromCode(String code) {
        return Arrays.stream(values())
                     .filter(type -> type.code.equalsIgnoreCase(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + code));
    }
}
