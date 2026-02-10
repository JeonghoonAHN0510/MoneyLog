package com.moneylog_backend.moneylog.payment.dto.res;

import com.moneylog_backend.global.type.PaymentEnum;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PaymentResDto {
    private Integer paymentId;
    private Integer userId;
    private Integer accountId;
    private String name;
    private PaymentEnum type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
