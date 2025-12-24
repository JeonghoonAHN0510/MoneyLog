package com.moneylog_backend.moneylog.ledger.dto;

import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.ledger.entity.FixedEntity;
import com.moneylog_backend.moneylog.user.entity.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FixedDto {
    private int fixed_id;
    private int user_id;
    private int category_id;
    private String title;
    private int amount;
    private int fixed_day;
    private LocalDate start_date;
    private LocalDate end_date;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public FixedEntity toEntity(UserEntity userEntity,
                                CategoryEntity categoryEntity){
        return FixedEntity.builder()
                .fixed_id(this.fixed_id)
                .userEntity(userEntity)
                .categoryEntity(categoryEntity)
                .title(this.title)
                .amount(this.amount)
                .fixed_day(this.fixed_day)
                .start_date(this.start_date)
                .end_date(this.end_date)
                .build();
    } // func end
} // class end