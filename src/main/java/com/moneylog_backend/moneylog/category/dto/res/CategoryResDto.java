package com.moneylog_backend.moneylog.category.dto.res;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.global.type.ColorEnum;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CategoryResDto {
    private Integer categoryId;
    private Integer userId;
    private String name;
    private CategoryEnum type;
    private ColorEnum color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
