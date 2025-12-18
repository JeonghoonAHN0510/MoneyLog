package com.moneylog_backend.moneylog.model.dto;

import com.moneylog_backend.moneylog.model.entity.BankEntity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankDto {
    private int bank_id;
    private String name;
    private String code;
    private String logo_image_url;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public BankEntity toEntity(){
        return BankEntity.builder()
                .bank_id(this.bank_id)
                .name(this.name)
                .code(this.code)
                .logo_image_url(this.logo_image_url)
                .build();
    } // func end
} // class end