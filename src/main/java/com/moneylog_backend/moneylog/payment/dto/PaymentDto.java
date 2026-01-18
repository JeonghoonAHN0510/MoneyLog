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
    private Integer payment_id;
    private Integer user_id;
    private String name;
    private PaymentEnum type;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public PaymentEntity toEntity () {
        return PaymentEntity.builder()
                            .payment_id(this.payment_id)
                            .user_id(this.user_id)
                            .name(this.name)
                            .type(this.type)
                            .build();
    }
}