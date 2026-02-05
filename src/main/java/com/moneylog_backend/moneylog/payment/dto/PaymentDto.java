package com.moneylog_backend.moneylog.payment.dto;

import com.moneylog_backend.global.type.PaymentEnum;
import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentDto {
    private Integer paymentId;
    private Integer userId;
    private Integer accountId;
    private String name;
    private PaymentEnum type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentEntity toEntity (Integer userId) {
        return PaymentEntity.builder()
                            .userId(userId)
                            .accountId(this.accountId)
                            .name(this.name)
                            .type(this.type)
                            .build();
    }
}