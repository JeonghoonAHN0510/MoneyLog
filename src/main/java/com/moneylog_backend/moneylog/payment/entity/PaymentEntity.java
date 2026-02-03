package com.moneylog_backend.moneylog.payment.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.global.type.PaymentEnum;
import com.moneylog_backend.moneylog.payment.dto.PaymentDto;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@Data
@Builder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", columnDefinition = "INT UNSIGNED")
    private Integer paymentId;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer userId;
    @Column(name = "accountId", columnDefinition = "INT UNSIGNED")
    private Integer accountId;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String name;
    @Column(columnDefinition = "ENUM('CASH', 'CREDIT_CARD', 'CHECK_CARD', 'BANK') NOT NULL")
    @Enumerated(EnumType.STRING)
    private PaymentEnum type;

    // todo 신용카드, 체크카드, 계좌이체면, Account_id 컬럼 추가해서 계좌 연결하기

    public PaymentDto toDto () {
        return PaymentDto.builder()
                         .paymentId(this.paymentId)
                         .userId(this.userId)
                         .accountId(this.accountId)
                         .name(this.name)
                         .type(this.type)
                         .createdAt(this.getCreatedAt())
                         .updatedAt(this.getUpdatedAt())
                         .build();
    }
}