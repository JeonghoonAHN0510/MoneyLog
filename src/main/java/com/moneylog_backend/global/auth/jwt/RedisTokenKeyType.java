package com.moneylog_backend.global.auth.jwt;

public enum RedisTokenKeyType {
    REFRESH_TOKEN("RT:"),
    BLACKLIST("BL:"),
    PASSWORD_RESET_OTP("PR:OTP:"),
    PASSWORD_RESET_OTP_ATTEMPTS("PR:OTP:ATTEMPTS:"),
    PASSWORD_RESET_OTP_RESEND("PR:OTP:RESEND:"),
    PASSWORD_RESET_TOKEN("PR:TOKEN:");

    private final String prefix;

    RedisTokenKeyType (String prefix) {
        this.prefix = prefix;
    }

    public String prefix () {
        return prefix;
    }
}
