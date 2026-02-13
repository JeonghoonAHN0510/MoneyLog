package com.moneylog_backend.global.type;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleEnum {
    USER("USER", "일반 사용자"),
    ADMIN("ADMIN", "관리자");

    private final String code;
    private final String label;

    public static RoleEnum fromCode(String code) {
        return Arrays.stream(values())
                     .filter(type -> type.code.equalsIgnoreCase(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + code));
    }
}
