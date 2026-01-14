package com.moneylog_backend.moneylog.category.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.moneylog_backend.moneylog.category.dto.CategoryDto;

@Mapper
public interface CategoryMapper {

    int checkCategoryNameTypeUnique (CategoryDto categoryDto);
}