package com.moneylog_backend.moneylog.budget.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.moneylog_backend.moneylog.budget.dto.BudgetDto;

@Mapper
public interface BudgetMapper {
    int checkCategoryAndUserIsDuplicate (int category_id, int user_id);

    List<BudgetDto> getBudgets (int user_id);
}