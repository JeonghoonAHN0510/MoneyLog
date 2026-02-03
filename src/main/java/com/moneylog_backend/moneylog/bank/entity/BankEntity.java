package com.moneylog_backend.moneylog.bank.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.moneylog.bank.dto.BankDto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bank")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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