package com.moneylog_backend.global.type;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryEnum {
    INCOME("INCOME", "수입"),
    EXPENSE("EXPENSE", "지출");

    private final String code;
    private final String label;

    private static final Map<String, CategoryEnum> CODE_MAP = Arrays.stream(values())
                                                                     .collect(Collectors.toMap(
                                                                         type -> type.code.toUpperCase(Locale.ROOT),
                                                                         type -> type));

    public static CategoryEnum fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Unknown category type: null");
        }
        CategoryEnum result = CODE_MAP.get(code.toUpperCase(Locale.ROOT));
        if (result == null) {
            throw new IllegalArgumentException("Unknown category type: " + code);
        }
        return result;
    }
}
