package com.moneylog_backend.moneylog.category.dto;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.global.type.ColorEnum;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryDto {
    private Integer categoryId;
    private Integer userId;
    private String name;
    private CategoryEnum type;
    private ColorEnum color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CategoryEntity toEntity (Integer userId) {
        return CategoryEntity.builder()
                             .userId(userId)
                             .name(this.name)
                             .type(this.type)
                             .color(this.color)
                             .build();
    }
}