package com.moneylog_backend.moneylog.payment.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.global.type.PaymentEnum;
import com.moneylog_backend.moneylog.payment.dto.PaymentDto;
import com.moneylog_backend.moneylog.user.entity.UserEntity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private int payment_id;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String name;
    @Column(columnDefinition = "ENUM('CASH', 'CREDIT_CARD', 'CHECK_CARD', 'BANK') NOT NULL")
    @Enumerated(EnumType.STRING)
    private PaymentEnum type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity userEntity;

    public PaymentDto toDto(){
        return PaymentDto.builder()
                .payment_id(this.payment_id)
                .name(this.name)
                .type(this.type)
                .user_id(this.userEntity != null ? this.userEntity.getUser_id() : 0)
                .created_at(this.getCreated_at())
                .updated_at(this.getUpdated_at())
                .build();
    } // func end
} // class end