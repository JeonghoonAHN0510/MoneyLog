package com.moneylog_backend.global.type;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ColorEnum {
    BLUE("#3b82f6"),
    RED("#ef4444"),
    GREEN("#22c55e"),
    YELLOW("#eab308"),
    PURPLE("#8b5cf6"),
    PINK("#ec4899"),
    CYAN("#06b6d4");

    private final String hexCode;

    // 1. [서버 -> 클라이언트] JSON으로 나갈 때 hexCode 값으로 출력 ("#3b82f6")
    @JsonValue
    public String getHexCode() {
        return hexCode;
    }

    // 2. [클라이언트 -> 서버] JSON으로 들어올 때 hexCode를 보고 Enum으로 변환
    @JsonCreator
    public static ColorEnum fromHexCode(String hexCode) {
        return Arrays.stream(ColorEnum.values())
                     .filter(color -> color.getHexCode().equalsIgnoreCase(hexCode))
                     .findFirst()
                     .orElse(ColorEnum.BLUE);
    }
}
