package com.moneylog_backend.moneylog.category.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.moneylog_backend.moneylog.category.dto.req.CategoryReqDto;

@Mapper
public interface CategoryMapper {

    int checkCategoryNameTypeUnique(CategoryReqDto categoryReqDto);

    String getCategoryTypeByCategoryId(int categoryId);
}