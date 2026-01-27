package com.moneylog_backend.global.type;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ColorEnum {
    RED("#ef4444"),
    AMBER("#f59e0b"),
    YELLOW("#eab308"),
    LIME("#84cc16"),
    GREEN("#22c55e"),
    EMERALD("#10b981"),
    TEAL("#14b8a6"),
    CYAN("#06b6d4"),
    BLUE("#3b82f6"),
    PURPLE("#8b5cf6"),
    PINK("#ec4899"),
    SLATE("#64748b");

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
