package com.moneylog_backend.moneylog.category.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.moneylog_backend.moneylog.fixed.dto.query.CheckCategoryNameTypeUniqueQuery;

@Mapper
public interface CategoryMapper {

    int checkCategoryNameTypeUnique(CheckCategoryNameTypeUniqueQuery selectQuery);

    String getCategoryTypeByCategoryId(int categoryId);
}