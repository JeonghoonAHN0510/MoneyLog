package com.moneylog_backend.moneylog.user.service;

import com.moneylog_backend.global.auth.jwt.RedisTokenKeyResolver;
import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.security.pii.PiiCryptoService;
import com.moneylog_backend.global.security.redis.RedisSecretProtector;
import com.moneylog_backend.global.type.ProviderEnum;
import com.moneylog_backend.global.util.InputStringNormalizer;
import com.moneylog_backend.global.util.RedisService;
import com.moneylog_backend.moneylog.user.dto.PasswordResetConfirmReqDto;
import com.moneylog_backend.moneylog.user.dto.PasswordResetRequestDto;
import com.moneylog_backend.moneylog.user.dto.PasswordResetRequestResponse;
import com.moneylog_backend.moneylog.user.dto.PasswordResetVerifyOtpReqDto;
import com.moneylog_backend.moneylog.user.dto.PasswordResetVerifyOtpResDto;
import com.moneylog_backend.moneylog.user.entity.UserEntity;
import com.moneylog_backend.moneylog.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final UserRepository userRepository;
    private final RedisService redisService;
    private final RedisTokenKeyResolver redisTokenKeyResolver;
    private final PasswordResetMailService passwordResetMailService;
    private final PasswordEncoder passwordEncoder;
    private final UserWriteTxService userWriteTxService;
    private final PiiCryptoService piiCryptoService;
    private final RedisSecretProtector redisSecretProtector;

    @Value("${app.security.password-reset.otp-ttl-seconds:300}")
    private int otpTtlSeconds;

    @Value("${app.security.password-reset.otp-max-attempts:5}")
    private int otpMaxAttempts;

    @Value("${app.security.password-reset.otp-resend-cooldown-seconds:60}")
    private int otpResendCooldownSeconds;

    @Value("${app.security.password-reset.reset-token-ttl-seconds:600}")
    private int resetTokenTtlSeconds;

    public PasswordResetRequestResponse requestOtp(PasswordResetRequestDto requestDto) {
        UserEntity userEntity = loadResettableUser(requestDto.getId(), requestDto.getEmail());
        Integer userId = userEntity.getUserId();

        String otpCode = generateOtpCode();
        boolean stored = storeOtpState(userId, otpCode);
        if (!stored) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, ErrorMessageConstants.PASSWORD_RESET_OTP_RESEND_COOLDOWN);
        }

        try {
            passwordResetMailService.sendOtp(userEntity.getEmail(), otpCode);
        } catch (RuntimeException ex) {
            clearOtpState(userId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageConstants.PASSWORD_RESET_EMAIL_SEND_FAILED);
        }

        return PasswordResetRequestResponse.builder()
                                           .sent(true)
                                           .otpTtlSeconds(otpTtlSeconds)
                                           .resendCooldownSeconds(otpResendCooldownSeconds)
                                           .build();
    }

    public PasswordResetVerifyOtpResDto verifyOtp(PasswordResetVerifyOtpReqDto requestDto) {
        UserEntity userEntity = loadResettableUser(requestDto.getId(), requestDto.getEmail());
        Integer userId = userEntity.getUserId();
        String resetToken = UUID.randomUUID().toString();
        RedisService.PasswordResetOtpVerifyResult verifyResult = redisService.verifyPasswordResetOtp(
            redisTokenKeyResolver.passwordResetOtp(userId),
            redisTokenKeyResolver.passwordResetOtpAttempts(userId),
            redisTokenKeyResolver.passwordResetOtpResend(userId),
            redisSecretProtector.hashPasswordResetOtp(userId, requestDto.getOtpCode()),
            otpMaxAttempts,
            Duration.ofSeconds(otpTtlSeconds),
            redisTokenKeyResolver.passwordResetToken(resetToken),
            String.valueOf(userId),
            Duration.ofSeconds(resetTokenTtlSeconds)
        );
        if (verifyResult == RedisService.PasswordResetOtpVerifyResult.EXPIRED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessageConstants.PASSWORD_RESET_OTP_EXPIRED);
        }
        if (verifyResult == RedisService.PasswordResetOtpVerifyResult.EXCEEDED) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, ErrorMessageConstants.PASSWORD_RESET_OTP_ATTEMPTS_EXCEEDED);
        }
        if (verifyResult == RedisService.PasswordResetOtpVerifyResult.INVALID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessageConstants.PASSWORD_RESET_OTP_INVALID);
        }

        return PasswordResetVerifyOtpResDto.builder()
                                           .resetToken(resetToken)
                                           .resetTokenTtlSeconds(resetTokenTtlSeconds)
                                           .build();
    }

    public void confirmReset(PasswordResetConfirmReqDto requestDto) {
        String tokenKey = redisTokenKeyResolver.passwordResetToken(requestDto.getResetToken());
        String storedUserId = redisService.getAndDeleteValue(tokenKey);

        if (storedUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessageConstants.PASSWORD_RESET_TOKEN_INVALID);
        }

        Integer userId = Integer.valueOf(storedUserId);
        UserEntity userEntity = userRepository.findById(userId)
                                              .orElseThrow(() -> new ResponseStatusException(
                                                  HttpStatus.NOT_FOUND,
                                                  ErrorMessageConstants.USER_NOT_FOUND
                                              ));

        String encodedPassword = passwordEncoder.encode(requestDto.getNewPassword());
        userWriteTxService.updatePassword(userId, encodedPassword);
        clearOtpState(userId);
        redisService.deleteValues(redisTokenKeyResolver.refreshToken(userEntity.getLoginId()));
    }

    private UserEntity loadResettableUser(String loginId, String email) {
        String normalizedLoginId = InputStringNormalizer.trimToNull(loginId);
        String normalizedEmail = piiCryptoService.normalizeEmail(email);

        UserEntity userEntity = userRepository.findByLoginId(normalizedLoginId)
                                              .orElseThrow(() -> new ResponseStatusException(
                                                  HttpStatus.BAD_REQUEST,
                                                  ErrorMessageConstants.PASSWORD_RESET_IDENTITY_CHECK_FAILED
                                              ));

        if (!piiCryptoService.normalizeEmail(userEntity.getEmail()).equals(normalizedEmail)) {
            throw invalidPasswordResetIdentity();
        }

        if (userEntity.getProvider() != null && userEntity.getProvider() != ProviderEnum.LOCAL) {
            throw invalidPasswordResetIdentity();
        }

        return userEntity;
    }

    private ResponseStatusException invalidPasswordResetIdentity() {
        return new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            ErrorMessageConstants.PASSWORD_RESET_IDENTITY_CHECK_FAILED
        );
    }

    private boolean storeOtpState(Integer userId, String otpCode) {
        return redisService.storePasswordResetOtpState(
            redisTokenKeyResolver.passwordResetOtp(userId),
            redisSecretProtector.hashPasswordResetOtp(userId, otpCode),
            Duration.ofSeconds(otpTtlSeconds),
            redisTokenKeyResolver.passwordResetOtpAttempts(userId),
            redisTokenKeyResolver.passwordResetOtpResend(userId),
            Duration.ofSeconds(otpResendCooldownSeconds)
        );
    }

    private void clearOtpState(Integer userId) {
        redisService.deleteValues(redisTokenKeyResolver.passwordResetOtp(userId));
        redisService.deleteValues(redisTokenKeyResolver.passwordResetOtpAttempts(userId));
        redisService.deleteValues(redisTokenKeyResolver.passwordResetOtpResend(userId));
    }

    private String generateOtpCode() {
        int number = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return "%06d".formatted(number);
    }
}
