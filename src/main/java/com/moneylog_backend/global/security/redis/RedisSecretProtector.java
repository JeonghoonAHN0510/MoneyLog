package com.moneylog_backend.global.security.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class RedisSecretProtector {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String REFRESH_TOKEN_NAMESPACE = "refresh:";
    private static final String BLACKLIST_TOKEN_NAMESPACE = "blacklist:";
    private static final String PASSWORD_RESET_TOKEN_NAMESPACE = "password-reset-token:";
    private static final String PASSWORD_RESET_OTP_NAMESPACE = "password-reset-otp:";

    private final String hashKeySource;

    public RedisSecretProtector(@Value("${app.security.redis-secret.hash-key:}") String hashKeySource) {
        this.hashKeySource = hashKeySource;
    }

    public String hashRefreshToken(String refreshToken) {
        return digest(REFRESH_TOKEN_NAMESPACE + refreshToken);
    }

    public boolean matchesRefreshToken(String rawRefreshToken, String storedHash) {
        return matches(hashRefreshToken(rawRefreshToken), storedHash);
    }

    public String hashBlacklistToken(String accessToken) {
        return digest(BLACKLIST_TOKEN_NAMESPACE + accessToken);
    }

    public String hashPasswordResetToken(String resetToken) {
        return digest(PASSWORD_RESET_TOKEN_NAMESPACE + resetToken);
    }

    public String hashPasswordResetOtp(Integer userId, String otpCode) {
        return digest(PASSWORD_RESET_OTP_NAMESPACE + userId + ":" + otpCode);
    }

    public boolean matchesPasswordResetOtp(Integer userId, String rawOtpCode, String storedHash) {
        return matches(hashPasswordResetOtp(userId, rawOtpCode), storedHash);
    }

    private boolean matches(String computedHash, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
            computedHash.getBytes(StandardCharsets.UTF_8),
            storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String digest(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(sha256Key(), HMAC_ALGORITHM));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Redis secret hash 생성에 실패했습니다.", ex);
        }
    }

    private byte[] sha256Key() {
        try {
            if (hashKeySource == null || hashKeySource.isBlank()) {
                throw new IllegalStateException("Redis secret hash key가 설정되지 않았습니다.");
            }
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return messageDigest.digest(hashKeySource.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Redis secret hash key 초기화에 실패했습니다.", ex);
        }
    }
}
