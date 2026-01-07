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
    @Column(columnDefinition = "INT UNSIGNED")
    private int bank_id;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String name;
    @Column(columnDefinition = "CHAR(3) NOT NULL UNIQUE")
    private String code;
    @Column(columnDefinition = "VARCHAR(255)")
    private String logo_image_url;

    public BankDto toDto () {
        return BankDto.builder()
                      .bank_id(this.bank_id)
                      .name(this.name)
                      .code(this.code)
                      .logo_image_url(this.logo_image_url)
                      .created_at(this.getCreated_at())
                      .updated_at(this.getUpdated_at())
                      .build();
    } // func end
} // class end