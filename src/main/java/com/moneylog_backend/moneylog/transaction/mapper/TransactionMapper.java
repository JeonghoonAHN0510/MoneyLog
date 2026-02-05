package com.moneylog_backend.moneylog.transaction.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.moneylog_backend.moneylog.transaction.dto.TransactionDto;
import com.moneylog_backend.moneylog.transaction.dto.query.SelectTransactionByUserIdQuery;

@Mapper
public interface TransactionMapper {
    List<TransactionDto> getTransactionsByUserId (SelectTransactionByUserIdQuery selectQuery);
}