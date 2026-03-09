package com.moneylog_backend.moneylog.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordResetVerifyOtpResDto {
    private String resetToken;
    private int resetTokenTtlSeconds;
}
