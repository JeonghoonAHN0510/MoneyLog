package com.moneylog_backend.moneylog.transaction.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.moneylog_backend.moneylog.transaction.dto.res.CategoryStatsResDto;
import com.moneylog_backend.moneylog.transaction.dto.res.DailySummaryResDto;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionResDto;
import com.moneylog_backend.moneylog.transaction.dto.query.SelectTransactionByUserIdQuery;

@Mapper
public interface TransactionMapper {
    List<TransactionResDto> getTransactionsByUserId(SelectTransactionByUserIdQuery selectQuery);

    List<DailySummaryResDto> getDailySummaries(SelectTransactionByUserIdQuery selectQuery);

    List<CategoryStatsResDto> getCategoryStats(SelectTransactionByUserIdQuery selectQuery);
}