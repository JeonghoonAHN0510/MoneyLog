package com.moneylog_backend.global.util;

public final class InputStringNormalizer {
    private InputStringNormalizer() {
    }

    public static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public static String trimToNull(String value) {
        String trimmed = trimToEmpty(value);
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String trimNullable(String value) {
        return value == null ? null : value.trim();
    }

    public static String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }
}
