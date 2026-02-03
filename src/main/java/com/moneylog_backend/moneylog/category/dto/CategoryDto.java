package com.moneylog_backend.moneylog.category.dto;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.global.type.ColorEnum;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;

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
    private Integer categoryId;
    private Integer userId;
    private String name;
    private CategoryEnum type;
    private ColorEnum color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CategoryEntity toEntity () {
        return CategoryEntity.builder()
                             .categoryId(this.categoryId)
                             .userId(this.userId)
                             .name(this.name)
                             .type(this.type)
                             .color(this.color)
                             .build();
    }
}