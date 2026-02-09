package com.moneylog_backend.moneylog.category.dto.req;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.global.type.ColorEnum;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryReqDto {
    private Integer categoryId;

    @NotBlank(message = "카테고리명은 필수입니다")
    @Size(max = 20, message = "카테고리명은 20자 이내여야 합니다")
    private String name;

    @NotNull(message = "카테고리 유형은 필수입니다")
    private CategoryEnum type;

    private ColorEnum color;

    public CategoryEntity toEntity(Integer userId) {
        return CategoryEntity.builder()
                             .userId(userId)
                             .name(this.name)
                             .type(this.type)
                             .color(this.color)
                             .build();
    }
}
