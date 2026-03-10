package com.moneylog_backend.global.auth.jwt;

import com.moneylog_backend.global.security.redis.RedisSecretProtector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisTokenKeyResolverTest {
    @Test
    void 블랙리스트_key에는_raw_access_token이_노출되지_않는다() {
        RedisTokenKeyResolver redisTokenKeyResolver = new RedisTokenKeyResolver(new RedisSecretProtector("redis-secret-key"));

        String key = redisTokenKeyResolver.blacklist("raw-access-token");

        assertTrue(key.startsWith("BL:"));
        assertFalse(key.contains("raw-access-token"));
    }

    @Test
    void reset_token_key에는_raw_reset_token이_노출되지_않는다() {
        RedisTokenKeyResolver redisTokenKeyResolver = new RedisTokenKeyResolver(new RedisSecretProtector("redis-secret-key"));

        String key = redisTokenKeyResolver.passwordResetToken("raw-reset-token");

        assertTrue(key.startsWith("PR:TOKEN:"));
        assertFalse(key.contains("raw-reset-token"));
    }
}
