package com.moneylog_backend.moneylog.user.service;

public interface PasswordResetMailService {
    void sendOtp(String email, String otpCode);
}
