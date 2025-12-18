package com.moneylog_backend.moneylog.model.dto;

import com.moneylog_backend.moneylog.model.entity.CategoryEntity;
import com.moneylog_backend.moneylog.model.entity.FixedExpenseEntity;
import com.moneylog_backend.moneylog.model.entity.UserEntity;

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
public class FixedExpenseDto {
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

    public FixedExpenseEntity toEntity(UserEntity userEntity,
                                       CategoryEntity categoryEntity){
        return FixedExpenseEntity.builder()
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