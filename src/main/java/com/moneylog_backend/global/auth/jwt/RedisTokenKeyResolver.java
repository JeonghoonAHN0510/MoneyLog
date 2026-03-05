package com.moneylog_backend.global.auth.jwt;

import org.springframework.stereotype.Component;

@Component
public class RedisTokenKeyResolver {
    public String refreshToken (String loginId) {
        return RedisTokenKeyType.REFRESH_TOKEN.prefix() + loginId;
    }

    public String blacklist (String accessToken) {
        return RedisTokenKeyType.BLACKLIST.prefix() + accessToken;
    }
}
