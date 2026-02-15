package com.moneylog_backend.global.type;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.moneylog_backend.global.constant.ErrorMessageConstants;

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

    private static final Map<String, StatusEnum> CODE_MAP = Arrays.stream(values())
                                                                   .collect(Collectors.toMap(
                                                                       type -> type.code.toUpperCase(Locale.ROOT),
                                                                       type -> type));

    public static StatusEnum fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException(ErrorMessageConstants.unknownStatus(null));
        }
        StatusEnum result = CODE_MAP.get(code.toUpperCase(Locale.ROOT));
        if (result == null) {
            throw new IllegalArgumentException(ErrorMessageConstants.unknownStatus(code));
        }
        return result;
    }
}
