package com.moneylog_backend.moneylog.payment.dto.req;

import com.moneylog_backend.global.type.PaymentEnum;
import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentReqDto {
    private Integer paymentId;
    private Integer accountId;

    @NotBlank(message = "결제수단명은 필수입니다")
    @Size(max = 30, message = "결제수단명은 30자 이내여야 합니다")
    private String name;

    @NotNull(message = "결제수단 유형은 필수입니다")
    private PaymentEnum type;

    public PaymentEntity toEntity(Integer userId) {
        return PaymentEntity.builder()
                            .userId(userId)
                            .accountId(this.accountId)
                            .name(this.name)
                            .type(this.type)
                            .build();
    }
}
