package com.moneylog_backend.global.auth.jwt;

import com.moneylog_backend.global.security.redis.RedisSecretProtector;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisTokenKeyResolver {
    private final RedisSecretProtector redisSecretProtector;

    public String refreshToken (String loginId) {
        return RedisTokenKeyType.REFRESH_TOKEN.prefix() + loginId;
    }

    public String blacklist (String accessToken) {
        return RedisTokenKeyType.BLACKLIST.prefix() + redisSecretProtector.hashBlacklistToken(accessToken);
    }

    public String passwordResetOtp(Integer userId) {
        return RedisTokenKeyType.PASSWORD_RESET_OTP.prefix() + userId;
    }

    public String passwordResetOtpAttempts(Integer userId) {
        return RedisTokenKeyType.PASSWORD_RESET_OTP_ATTEMPTS.prefix() + userId;
    }

    public String passwordResetOtpResend(Integer userId) {
        return RedisTokenKeyType.PASSWORD_RESET_OTP_RESEND.prefix() + userId;
    }

    public String passwordResetToken(String resetToken) {
        return RedisTokenKeyType.PASSWORD_RESET_TOKEN.prefix() + redisSecretProtector.hashPasswordResetToken(resetToken);
    }
}
