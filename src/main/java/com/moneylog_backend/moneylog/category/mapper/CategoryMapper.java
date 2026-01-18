package com.moneylog_backend.moneylog.category.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.moneylog.category.dto.CategoryDto;

@Mapper
public interface CategoryMapper {

    int checkCategoryNameTypeUnique (CategoryDto categoryDto);

    String getCategoryTypeByCategoryId(int category_id);
}