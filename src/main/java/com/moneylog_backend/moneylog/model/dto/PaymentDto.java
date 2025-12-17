package com.moneylog_backend.moneylog.model.dto;

import com.moneylog_backend.moneylog.common.type.PaymentType;
import com.moneylog_backend.moneylog.model.entity.PaymentEntity;
import com.moneylog_backend.moneylog.model.entity.UserEntity;

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
    private int payment_id;
    private int user_id;
    private String name;
    private PaymentType type;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public PaymentEntity toEntity(UserEntity userEntity){
        return PaymentEntity.builder()
                .payment_id(this.payment_id)
                .userEntity(userEntity)
                .name(this.name)
                .type(this.type)
                .build();
    } // func end
} // class end