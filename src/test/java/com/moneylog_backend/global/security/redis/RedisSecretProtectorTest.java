package com.moneylog_backend.global.security.redis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisSecretProtectorTest {
    @Test
    void 같은_refresh_token은_같은_hash를_생성한다() {
        RedisSecretProtector redisSecretProtector = new RedisSecretProtector("redis-secret-key");

        String first = redisSecretProtector.hashRefreshToken("refresh-token");
        String second = redisSecretProtector.hashRefreshToken("refresh-token");

        assertEquals(first, second);
        assertNotEquals("refresh-token", first);
        assertTrue(redisSecretProtector.matchesRefreshToken("refresh-token", first));
    }

    @Test
    void otp_hash는_userId를_함께_반영한다() {
        RedisSecretProtector redisSecretProtector = new RedisSecretProtector("redis-secret-key");

        String first = redisSecretProtector.hashPasswordResetOtp(1, "123456");
        String second = redisSecretProtector.hashPasswordResetOtp(2, "123456");

        assertNotEquals(first, second);
        assertTrue(redisSecretProtector.matchesPasswordResetOtp(1, "123456", first));
        assertFalse(redisSecretProtector.matchesPasswordResetOtp(2, "123456", first));
    }

    @Test
    void key가_없으면_hash_생성시_실패한다() {
        RedisSecretProtector redisSecretProtector = new RedisSecretProtector("");

        assertThrows(IllegalStateException.class, () -> redisSecretProtector.hashRefreshToken("refresh-token"));
    }
}
