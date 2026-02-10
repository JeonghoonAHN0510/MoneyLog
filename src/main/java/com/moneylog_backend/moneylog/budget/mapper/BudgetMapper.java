package com.moneylog_backend.moneylog.budget.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.moneylog_backend.moneylog.budget.dto.res.BudgetResDto;

@Mapper
public interface BudgetMapper {
    int checkCategoryAndUserIsDuplicate(int categoryId, int userId);

    List<BudgetResDto> getBudgets(int userId);
}