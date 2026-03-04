package com.moneylog_backend.moneylog.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshReqDto {
    @NotBlank(message = "리프레시 토큰을 입력해주세요")
    private String refreshToken;
}
