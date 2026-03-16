package com.moneylog_backend.moneylog.user.service;

import com.moneylog_backend.global.auth.jwt.RedisTokenKeyResolver;
import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.security.pii.PiiCryptoService;
import com.moneylog_backend.global.security.redis.RedisSecretProtector;
import com.moneylog_backend.global.type.ProviderEnum;
import com.moneylog_backend.global.util.RedisService;
import com.moneylog_backend.moneylog.user.dto.PasswordResetConfirmReqDto;
import com.moneylog_backend.moneylog.user.dto.PasswordResetRequestDto;
import com.moneylog_backend.moneylog.user.dto.PasswordResetRequestResponse;
import com.moneylog_backend.moneylog.user.dto.PasswordResetVerifyOtpReqDto;
import com.moneylog_backend.moneylog.user.dto.PasswordResetVerifyOtpResDto;
import com.moneylog_backend.moneylog.user.entity.UserEntity;
import com.moneylog_backend.moneylog.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RedisService redisService;
    @Mock
    private RedisTokenKeyResolver redisTokenKeyResolver;
    @Mock
    private PasswordResetMailService passwordResetMailService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserWriteTxService userWriteTxService;
    @Mock
    private PiiCryptoService piiCryptoService;
    @Mock
    private RedisSecretProtector redisSecretProtector;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "otpTtlSeconds", 300);
        ReflectionTestUtils.setField(passwordResetService, "otpMaxAttempts", 5);
        ReflectionTestUtils.setField(passwordResetService, "otpResendCooldownSeconds", 60);
        ReflectionTestUtils.setField(passwordResetService, "resetTokenTtlSeconds", 600);
        lenient().when(piiCryptoService.normalizeEmail(anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0, String.class);
            return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        });
    }

    @Test
    void 인증번호_요청_성공시_otp를_저장하고_메일을_전송한다() {
        UserEntity userEntity = localUser();
        when(userRepository.findByLoginId("tester")).thenReturn(Optional.of(userEntity));
        when(redisTokenKeyResolver.passwordResetOtp(1)).thenReturn("PR:OTP:1");
        when(redisTokenKeyResolver.passwordResetOtpAttempts(1)).thenReturn("PR:OTP:ATTEMPTS:1");
        when(redisTokenKeyResolver.passwordResetOtpResend(1)).thenReturn("PR:OTP:RESEND:1");
        when(redisSecretProtector.hashPasswordResetOtp(eq(1), anyString())).thenReturn("otp-hash");
        when(redisService.storePasswordResetOtpState(
            "PR:OTP:1",
            "otp-hash",
            Duration.ofSeconds(300),
            "PR:OTP:ATTEMPTS:1",
            "PR:OTP:RESEND:1",
            Duration.ofSeconds(60)
        )).thenReturn(true);

        PasswordResetRequestResponse response = passwordResetService.requestOtp(
            new PasswordResetRequestDto("tester", "tester@moneylog.com")
        );

        assertEquals(true, response.isSent());
        assertEquals(300, response.getOtpTtlSeconds());
        assertEquals(60, response.getResendCooldownSeconds());
        verify(redisService).storePasswordResetOtpState(
            "PR:OTP:1",
            "otp-hash",
            Duration.ofSeconds(300),
            "PR:OTP:ATTEMPTS:1",
            "PR:OTP:RESEND:1",
            Duration.ofSeconds(60)
        );
        verify(passwordResetMailService).sendOtp(eq("tester@moneylog.com"), any());
    }

    @Test
    void 인증번호_요청시_아이디와_이메일의_앞뒤_공백을_정리한다() {
        UserEntity userEntity = localUser();
        when(userRepository.findByLoginId("tester")).thenReturn(Optional.of(userEntity));
        when(redisTokenKeyResolver.passwordResetOtp(1)).thenReturn("PR:OTP:1");
        when(redisTokenKeyResolver.passwordResetOtpAttempts(1)).thenReturn("PR:OTP:ATTEMPTS:1");
        when(redisTokenKeyResolver.passwordResetOtpResend(1)).thenReturn("PR:OTP:RESEND:1");
        when(redisSecretProtector.hashPasswordResetOtp(eq(1), anyString())).thenReturn("otp-hash");
        when(redisService.storePasswordResetOtpState(
            "PR:OTP:1",
            "otp-hash",
            Duration.ofSeconds(300),
            "PR:OTP:ATTEMPTS:1",
            "PR:OTP:RESEND:1",
            Duration.ofSeconds(60)
        )).thenReturn(true);

        passwordResetService.requestOtp(new PasswordResetRequestDto("  tester  ", "  TESTER@moneylog.com  "));

        verify(userRepository).findByLoginId("tester");
        verify(passwordResetMailService).sendOtp(eq("tester@moneylog.com"), any());
    }

    @Test
    void 인증번호_요청시_아이디가_없어도_일반화된_400을_반환한다() {
        when(userRepository.findByLoginId("tester")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> passwordResetService.requestOtp(new PasswordResetRequestDto("tester", "tester@moneylog.com"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessageConstants.PASSWORD_RESET_IDENTITY_CHECK_FAILED, exception.getReason());
        verify(redisService, never()).storePasswordResetOtpState(anyString(), anyString(), any(), anyString(), anyString(), any());
        verify(passwordResetMailService, never()).sendOtp(any(), any());
    }

    @Test
    void 인증번호_요청시_이메일이_불일치해도_일반화된_400을_반환한다() {
        when(userRepository.findByLoginId("tester")).thenReturn(Optional.of(localUser()));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> passwordResetService.requestOtp(new PasswordResetRequestDto("tester", "other@moneylog.com"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessageConstants.PASSWORD_RESET_IDENTITY_CHECK_FAILED, exception.getReason());
        verify(redisService, never()).storePasswordResetOtpState(anyString(), anyString(), any(), anyString(), anyString(), any());
        verify(passwordResetMailService, never()).sendOtp(any(), any());
    }

    @Test
    void 인증번호_요청시_LOCAL이_아닌_계정도_일반화된_400을_반환한다() {
        when(userRepository.findByLoginId("tester")).thenReturn(Optional.of(oauthUser()));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> passwordResetService.requestOtp(new PasswordResetRequestDto("tester", "tester@moneylog.com"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessageConstants.PASSWORD_RESET_IDENTITY_CHECK_FAILED, exception.getReason());
        verify(redisService, never()).storePasswordResetOtpState(anyString(), anyString(), any(), anyString(), anyString(), any());
        verify(passwordResetMailService, never()).sendOtp(any(), any());
    }

    @Test
    void 인증번호_재전송_쿨다운중이면_429를_반환한다() {
        when(userRepository.findByLoginId("tester")).thenReturn(Optional.of(localUser()));
        when(redisTokenKeyResolver.passwordResetOtp(1)).thenReturn("PR:OTP:1");
        when(redisTokenKeyResolver.passwordResetOtpAttempts(1)).thenReturn("PR:OTP:ATTEMPTS:1");
        when(redisTokenKeyResolver.passwordResetOtpResend(1)).thenReturn("PR:OTP:RESEND:1");
        when(redisSecretProtector.hashPasswordResetOtp(eq(1), anyString())).thenReturn("otp-hash");
        when(redisService.storePasswordResetOtpState(
            "PR:OTP:1",
            "otp-hash",
            Duration.ofSeconds(300),
            "PR:OTP:ATTEMPTS:1",
            "PR:OTP:RESEND:1",
            Duration.ofSeconds(60)
        )).thenReturn(false);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> passwordResetService.requestOtp(new PasswordResetRequestDto("tester", "tester@moneylog.com"))
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
        assertEquals(ErrorMessageConstants.PASSWORD_RESET_OTP_RESEND_COOLDOWN, exception.getReason());
        verify(passwordResetMailService, never()).sendOtp(any(), any());
    }

    @Test
    void 인증번호_검증_성공시_resetToken을_발급한다() {
        UserEntity userEntity = localUser();
        when(userRepository.findByLoginId("tester")).thenReturn(Optional.of(userEntity));
        when(redisTokenKeyResolver.passwordResetOtp(1)).thenReturn("PR:OTP:1");
        when(redisTokenKeyResolver.passwordResetOtpAttempts(1)).thenReturn("PR:OTP:ATTEMPTS:1");
        when(redisTokenKeyResolver.passwordResetOtpResend(1)).thenReturn("PR:OTP:RESEND:1");
        when(redisTokenKeyResolver.passwordResetToken(anyString())).thenAnswer(invocation -> "PR:TOKEN:" + invocation.getArgument(0));
        when(redisSecretProtector.hashPasswordResetOtp(1, "123456")).thenReturn("otp-hash");
        when(redisService.verifyPasswordResetOtp(
            eq("PR:OTP:1"),
            eq("PR:OTP:ATTEMPTS:1"),
            eq("PR:OTP:RESEND:1"),
            eq("otp-hash"),
            eq(5),
            eq(Duration.ofSeconds(300)),
            anyString(),
            eq("1"),
            eq(Duration.ofSeconds(600))
        )).thenReturn(RedisService.PasswordResetOtpVerifyResult.SUCCESS);

        PasswordResetVerifyOtpResDto response = passwordResetService.verifyOtp(
            new PasswordResetVerifyOtpReqDto("tester", "tester@moneylog.com", "123456")
        );

        assertNotNull(response.getResetToken());
        assertEquals(600, response.getResetTokenTtlSeconds());
        verify(redisService).verifyPasswordResetOtp(
            eq("PR:OTP:1"),
            eq("PR:OTP:ATTEMPTS:1"),
            eq("PR:OTP:RESEND:1"),
            eq("otp-hash"),
            eq(5),
            eq(Duration.ofSeconds(300)),
            eq("PR:TOKEN:" + response.getResetToken()),
            eq("1"),
            eq(Duration.ofSeconds(600))
        );
    }

    @Test
    void 인증번호가_틀리면_시도횟수를_증가시킨다() {
        UserEntity userEntity = localUser();
        when(userRepository.findByLoginId("tester")).thenReturn(Optional.of(userEntity));
        when(redisTokenKeyResolver.passwordResetOtp(1)).thenReturn("PR:OTP:1");
        when(redisTokenKeyResolver.passwordResetOtpAttempts(1)).thenReturn("PR:OTP:ATTEMPTS:1");
        when(redisTokenKeyResolver.passwordResetOtpResend(1)).thenReturn("PR:OTP:RESEND:1");
        when(redisTokenKeyResolver.passwordResetToken(anyString())).thenAnswer(invocation -> "PR:TOKEN:" + invocation.getArgument(0));
        when(redisSecretProtector.hashPasswordResetOtp(1, "000000")).thenReturn("otp-hash");
        when(redisService.verifyPasswordResetOtp(
            eq("PR:OTP:1"),
            eq("PR:OTP:ATTEMPTS:1"),
            eq("PR:OTP:RESEND:1"),
            eq("otp-hash"),
            eq(5),
            eq(Duration.ofSeconds(300)),
            anyString(),
            eq("1"),
            eq(Duration.ofSeconds(600))
        )).thenReturn(RedisService.PasswordResetOtpVerifyResult.INVALID);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> passwordResetService.verifyOtp(new PasswordResetVerifyOtpReqDto("tester", "tester@moneylog.com", "000000"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessageConstants.PASSWORD_RESET_OTP_INVALID, exception.getReason());
    }

    @Test
    void 인증번호_검증시_아이디가_없어도_일반화된_400을_반환한다() {
        when(userRepository.findByLoginId("tester")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> passwordResetService.verifyOtp(new PasswordResetVerifyOtpReqDto("tester", "tester@moneylog.com", "123456"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessageConstants.PASSWORD_RESET_IDENTITY_CHECK_FAILED, exception.getReason());
        verify(redisService, never()).getValues(anyString());
    }

    @Test
    void 인증번호_검증시_이메일이_불일치해도_일반화된_400을_반환한다() {
        when(userRepository.findByLoginId("tester")).thenReturn(Optional.of(localUser()));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> passwordResetService.verifyOtp(new PasswordResetVerifyOtpReqDto("tester", "other@moneylog.com", "123456"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessageConstants.PASSWORD_RESET_IDENTITY_CHECK_FAILED, exception.getReason());
        verify(redisService, never()).getValues(anyString());
    }

    @Test
    void 인증번호_검증시_LOCAL이_아닌_계정도_일반화된_400을_반환한다() {
        when(userRepository.findByLoginId("tester")).thenReturn(Optional.of(oauthUser()));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> passwordResetService.verifyOtp(new PasswordResetVerifyOtpReqDto("tester", "tester@moneylog.com", "123456"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessageConstants.PASSWORD_RESET_IDENTITY_CHECK_FAILED, exception.getReason());
        verify(redisService, never()).getValues(anyString());
    }

    @Test
    void 비밀번호_재설정_확정시_비밀번호를_변경하고_refreshToken을_삭제한다() {
        UserEntity userEntity = localUser();
        when(redisTokenKeyResolver.passwordResetToken("reset-token")).thenReturn("PR:TOKEN:reset-token");
        when(redisService.getAndDeleteValue("PR:TOKEN:reset-token")).thenReturn("1");
        when(userRepository.findById(1)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-password");
        when(redisTokenKeyResolver.passwordResetOtp(1)).thenReturn("PR:OTP:1");
        when(redisTokenKeyResolver.passwordResetOtpAttempts(1)).thenReturn("PR:OTP:ATTEMPTS:1");
        when(redisTokenKeyResolver.passwordResetOtpResend(1)).thenReturn("PR:OTP:RESEND:1");
        when(redisTokenKeyResolver.refreshToken("tester")).thenReturn("RT:tester");

        passwordResetService.confirmReset(new PasswordResetConfirmReqDto("reset-token", "new-password"));

        verify(userWriteTxService).updatePassword(1, "encoded-password");
        verify(redisService).getAndDeleteValue("PR:TOKEN:reset-token");
        verify(redisService).deleteValues("RT:tester");
    }

    @Test
    void 비밀번호_재설정_확정시_resetToken이_없으면_400을_반환한다() {
        when(redisTokenKeyResolver.passwordResetToken("reset-token")).thenReturn("PR:TOKEN:reset-token");
        when(redisService.getAndDeleteValue("PR:TOKEN:reset-token")).thenReturn(null);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> passwordResetService.confirmReset(new PasswordResetConfirmReqDto("reset-token", "new-password"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessageConstants.PASSWORD_RESET_TOKEN_INVALID, exception.getReason());
        verify(userWriteTxService, never()).updatePassword(any(), anyString());
    }

    private UserEntity localUser() {
        return UserEntity.builder()
                         .userId(1)
                         .loginId("tester")
                         .email("tester@moneylog.com")
                         .provider(ProviderEnum.LOCAL)
                         .createdAt(LocalDateTime.now())
                         .updatedAt(LocalDateTime.now())
                         .build();
    }

    private UserEntity oauthUser() {
        return UserEntity.builder()
                         .userId(2)
                         .loginId("tester")
                         .email("tester@moneylog.com")
                         .provider(ProviderEnum.KAKAO)
                         .createdAt(LocalDateTime.now())
                         .updatedAt(LocalDateTime.now())
                         .build();
    }
}
