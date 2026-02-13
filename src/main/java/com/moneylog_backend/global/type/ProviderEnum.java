package com.moneylog_backend.global.type;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProviderEnum {
    LOCAL("LOCAL", "로컬"),
    KAKAO("KAKAO", "카카오"),
    GOOGLE("GOOGLE", "구글");

    private final String code;
    private final String label;

    public static ProviderEnum fromCode(String code) {
        return Arrays.stream(values())
                     .filter(type -> type.code.equalsIgnoreCase(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown provider: " + code));
    }
}
