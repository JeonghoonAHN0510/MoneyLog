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
public enum ProviderEnum {
    LOCAL("LOCAL", "로컬"),
    KAKAO("KAKAO", "카카오"),
    GOOGLE("GOOGLE", "구글");

    private final String code;
    private final String label;

    private static final Map<String, ProviderEnum> CODE_MAP = Arrays.stream(values())
                                                                     .collect(Collectors.toMap(
                                                                         type -> type.code.toUpperCase(Locale.ROOT),
                                                                         type -> type));

    public static ProviderEnum fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException(ErrorMessageConstants.unknownProvider(null));
        }
        ProviderEnum result = CODE_MAP.get(code.toUpperCase(Locale.ROOT));
        if (result == null) {
            throw new IllegalArgumentException(ErrorMessageConstants.unknownProvider(code));
        }
        return result;
    }
}
