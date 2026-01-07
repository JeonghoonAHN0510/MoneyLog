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
    private int payment_id;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private int user_id;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String name;
    @Column(columnDefinition = "ENUM('CASH', 'CREDIT_CARD', 'CHECK_CARD', 'BANK') NOT NULL")
    @Enumerated(EnumType.STRING)
    private PaymentEnum type;

    public PaymentDto toDto () {
        return PaymentDto.builder()
                         .payment_id(this.payment_id)
                         .user_id(this.user_id)
                         .name(this.name)
                         .type(this.type)
                         .created_at(this.getCreated_at())
                         .updated_at(this.getUpdated_at())
                         .build();
    } // func end
} // class end