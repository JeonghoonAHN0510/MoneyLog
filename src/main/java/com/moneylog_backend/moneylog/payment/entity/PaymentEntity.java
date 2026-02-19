package com.moneylog_backend.moneylog.payment.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.global.type.PaymentEnum;
import com.moneylog_backend.moneylog.payment.dto.res.PaymentResDto;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "payment")
@Getter
@SuperBuilder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", columnDefinition = "INT UNSIGNED")
    private Integer paymentId;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer userId;
    @Column(name = "account_id", columnDefinition = "INT UNSIGNED")
    private Integer accountId;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String name;
    @Column(columnDefinition = "ENUM('CASH', 'CREDIT_CARD', 'CHECK_CARD', 'BANK') NOT NULL")
    @Enumerated(EnumType.STRING)
    private PaymentEnum type;

    public PaymentResDto toDto() {
        return PaymentResDto.builder()
                         .paymentId(this.paymentId)
                         .userId(this.userId)
                         .accountId(this.accountId)
                         .name(this.name)
                         .type(this.type)
                         .createdAt(this.getCreatedAt())
                         .updatedAt(this.getUpdatedAt())
                         .build();
    }

    public void updateDetails(Integer accountId, String name, PaymentEnum type) {
        this.accountId = accountId;

        if (name != null && !name.isEmpty()) {
            this.name = name;
        }

        if (type != null) {
            this.type = type;
        }
    }
}
