package com.moneylog_backend.moneylog.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordResetRequestResponse {
    private boolean sent;
    private int otpTtlSeconds;
    private int resendCooldownSeconds;
}
