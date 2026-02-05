package com.moneylog_backend.moneylog.bank.entity;

import org.hibernate.annotations.DynamicInsert;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.moneylog.bank.dto.BankDto;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "bank")
@Getter
@SuperBuilder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BankEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_id", columnDefinition = "INT UNSIGNED")
    private Integer bankId;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String name;
    @Column(columnDefinition = "CHAR(3) NOT NULL UNIQUE")
    private String code;
    @Column(name = "logo_image_url", columnDefinition = "VARCHAR(255)")
    private String logoImageUrl;

    public BankDto toDto () {
        return BankDto.builder()
                      .bankId(this.bankId)
                      .name(this.name)
                      .code(this.code)
                      .logoImageUrl(this.logoImageUrl)
                      .createdAt(this.getCreatedAt())
                      .updatedAt(this.getUpdatedAt())
                      .build();
    }
}