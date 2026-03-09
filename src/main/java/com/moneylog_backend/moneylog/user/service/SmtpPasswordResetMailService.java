package com.moneylog_backend.moneylog.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SmtpPasswordResetMailService implements PasswordResetMailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.password-reset.from:}")
    private String fromAddress;

    @Value("${app.mail.password-reset.subject:[MoneyLog] 비밀번호 재설정 인증번호}")
    private String subject;

    @Value("${app.security.password-reset.otp-ttl-seconds:300}")
    private int otpTtlSeconds;

    @Override
    public void sendOtp(String email, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setTo(email);
        message.setSubject(subject);
        message.setText("""
                        MoneyLog 비밀번호 재설정 인증번호입니다.

                        인증번호: %s
                        유효시간: %d분

                        본인이 요청하지 않았다면 이 메일을 무시해주세요.
                        """.formatted(otpCode, Math.max(1, otpTtlSeconds / 60)));
        mailSender.send(message);
    }
}
