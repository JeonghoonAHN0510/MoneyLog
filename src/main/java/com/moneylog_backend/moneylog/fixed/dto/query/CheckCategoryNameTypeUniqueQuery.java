package com.moneylog_backend.moneylog.fixed.dto.query;

import com.moneylog_backend.global.type.CategoryEnum;

import lombok.Builder;

@Builder
public class CheckCategoryNameTypeUniqueQuery {
    private Integer userId;
    private String name;
    private CategoryEnum type;
}
