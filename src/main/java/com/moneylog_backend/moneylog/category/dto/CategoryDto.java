package com.moneylog_backend.moneylog.category.dto;

import com.moneylog_backend.global.type.CategoryType;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.user.entity.UserEntity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private int category_id;
    private int user_id;
    private String name;
    private CategoryType type;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public CategoryEntity toEntity(UserEntity userEntity){
        return CategoryEntity.builder()
                .category_id(this.category_id)
                .userEntity(userEntity)
                .name(this.name)
                .type(this.type)
                .build();
    } // func end
} // class end