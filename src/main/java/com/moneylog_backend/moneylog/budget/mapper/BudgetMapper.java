package com.moneylog_backend.moneylog.budget.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BudgetMapper {
    int checkCategoryAndUserIsDuplicate(int category_id, int user_id);
}