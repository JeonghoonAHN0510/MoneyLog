package com.moneylog_backend.moneylog.payment.dto;

import com.moneylog_backend.global.type.PaymentEnum;
import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private Integer paymentId;
    private Integer userId;
    private Integer accountId;
    private String name;
    private PaymentEnum type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentEntity toEntity () {
        return PaymentEntity.builder()
                            .paymentId(this.paymentId)
                            .userId(this.userId)
                            .accountId(this.accountId)
                            .name(this.name)
                            .type(this.type)
                            .build();
    }
}