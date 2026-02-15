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
public enum AccountTypeEnum {
    BANK("BANK", "은행"), CASH("CASH", "현금"), POINT("POINT", "포인트"), OTHER("OTHER", "기타");

    private final String code;
    private final String label;

    private static final Map<String, AccountTypeEnum> CODE_MAP = Arrays.stream(values())
                                                                       .collect(Collectors.toMap(
                                                                           type -> type.code.toUpperCase(Locale.ROOT),
                                                                           type -> type));

    public static AccountTypeEnum fromCode (String code) {
        if (code == null) {
            throw new IllegalArgumentException(ErrorMessageConstants.unknownAccountType(null));
        }
        AccountTypeEnum result = CODE_MAP.get(code.toUpperCase(Locale.ROOT));
        if (result == null) {
            throw new IllegalArgumentException(ErrorMessageConstants.unknownAccountType(code));
        }
        return result;
    }
}
