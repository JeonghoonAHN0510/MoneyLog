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
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer payment_id;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer user_id;
    @Column(name = "account_id", columnDefinition = "INT UNSIGNED")
    private Integer account_id;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String name;
    @Column(columnDefinition = "ENUM('CASH', 'CREDIT_CARD', 'CHECK_CARD', 'BANK') NOT NULL")
    @Enumerated(EnumType.STRING)
    private PaymentEnum type;

    // todo 신용카드, 체크카드, 계좌이체면, Account_id 컬럼 추가해서 계좌 연결하기

    public PaymentDto toDto () {
        return PaymentDto.builder()
                         .payment_id(this.payment_id)
                         .user_id(this.user_id)
                         .account_id(this.account_id)
                         .name(this.name)
                         .type(this.type)
                         .created_at(this.getCreated_at())
                         .updated_at(this.getUpdated_at())
                         .build();
    }
}