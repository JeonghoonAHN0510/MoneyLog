package com.moneylog_backend.global.util;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {
    public enum PasswordResetOtpVerifyResult {
        SUCCESS,
        EXPIRED,
        INVALID,
        EXCEEDED
    }

    private static final DefaultRedisScript<Long> COMPARE_AND_SET_SCRIPT = new DefaultRedisScript<>(
        """
        local current = redis.call('GET', KEYS[1])
        if current ~= ARGV[1] then
            return 0
        end
        redis.call('SET', KEYS[1], ARGV[2], 'PX', ARGV[3])
        return 1
        """,
        Long.class
    );
    private static final DefaultRedisScript<String> CONSUME_ONCE_SCRIPT = new DefaultRedisScript<>(
        """
        local current = redis.call('GET', KEYS[1])
        if not current then
            return nil
        end
        redis.call('DEL', KEYS[1])
        return current
        """,
        String.class
    );
    private static final DefaultRedisScript<Long> STORE_PASSWORD_RESET_OTP_SCRIPT = new DefaultRedisScript<>(
        """
        if redis.call('EXISTS', KEYS[3]) == 1 then
            return 0
        end
        redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[2])
        redis.call('SET', KEYS[2], '0', 'PX', ARGV[2])
        redis.call('SET', KEYS[3], '1', 'PX', ARGV[3])
        return 1
        """,
        Long.class
    );
    private static final DefaultRedisScript<String> VERIFY_PASSWORD_RESET_OTP_SCRIPT = new DefaultRedisScript<>(
        """
        local storedOtp = redis.call('GET', KEYS[1])
        if not storedOtp then
            return 'EXPIRED'
        end

        local attempts = tonumber(redis.call('GET', KEYS[2]) or '0')
        local maxAttempts = tonumber(ARGV[2])
        if attempts >= maxAttempts then
            redis.call('DEL', KEYS[1], KEYS[2], KEYS[3])
            return 'EXCEEDED'
        end

        if storedOtp ~= ARGV[1] then
            local nextAttempts = attempts + 1
            if nextAttempts >= maxAttempts then
                redis.call('DEL', KEYS[1], KEYS[2], KEYS[3])
                return 'EXCEEDED'
            end
            redis.call('SET', KEYS[2], tostring(nextAttempts), 'PX', ARGV[3])
            return 'INVALID'
        end

        redis.call('DEL', KEYS[1], KEYS[2], KEYS[3])
        redis.call('SET', KEYS[4], ARGV[4], 'PX', ARGV[5])
        return 'SUCCESS'
        """,
        String.class
    );

    private final RedisTemplate<String, Object> redisTemplate;

    // 데이터 저장 (Key, Value, 만료시간)
    public void setValues (String key, String value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public boolean setValueIfAbsent (String key, String value, Duration duration) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, duration));
    }

    // 데이터 조회
    public String getValues (String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : value.toString();
    }

    // 데이터 삭제
    public void deleteValues (String key) {
        redisTemplate.delete(key);
    }

    public boolean compareAndSetValues (String key, String expectedValue, String newValue, Duration duration) {
        Long result = redisTemplate.execute(
            COMPARE_AND_SET_SCRIPT,
            List.of(key),
            expectedValue,
            newValue,
            String.valueOf(duration.toMillis())
        );
        return Long.valueOf(1L).equals(result);
    }

    public String getAndDeleteValue (String key) {
        return redisTemplate.execute(CONSUME_ONCE_SCRIPT, List.of(key));
    }

    public boolean storePasswordResetOtpState (
        String otpKey,
        String otpHash,
        Duration otpDuration,
        String attemptsKey,
        String resendKey,
        Duration resendDuration
    ) {
        Long result = redisTemplate.execute(
            STORE_PASSWORD_RESET_OTP_SCRIPT,
            List.of(otpKey, attemptsKey, resendKey),
            otpHash,
            String.valueOf(otpDuration.toMillis()),
            String.valueOf(resendDuration.toMillis())
        );
        return Long.valueOf(1L).equals(result);
    }

    public PasswordResetOtpVerifyResult verifyPasswordResetOtp (
        String otpKey,
        String attemptsKey,
        String resendKey,
        String submittedOtpHash,
        int maxAttempts,
        Duration attemptsDuration,
        String resetTokenKey,
        String userId,
        Duration resetTokenDuration
    ) {
        String result = redisTemplate.execute(
            VERIFY_PASSWORD_RESET_OTP_SCRIPT,
            List.of(otpKey, attemptsKey, resendKey, resetTokenKey),
            submittedOtpHash,
            String.valueOf(maxAttempts),
            String.valueOf(attemptsDuration.toMillis()),
            userId,
            String.valueOf(resetTokenDuration.toMillis())
        );

        if (result == null || result.isBlank()) {
            return PasswordResetOtpVerifyResult.EXPIRED;
        }

        return PasswordResetOtpVerifyResult.valueOf(result);
    }

    // 키 존재 여부 확인 (블랙리스트 체크용)
    public boolean hasKey (String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
