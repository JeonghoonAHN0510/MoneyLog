package com.moneylog_backend.global.auth.jwt;

public enum RedisTokenKeyType {
    REFRESH_TOKEN("RT:"),
    BLACKLIST("BL:");

    private final String prefix;

    RedisTokenKeyType (String prefix) {
        this.prefix = prefix;
    }

    public String prefix () {
        return prefix;
    }
}
