package com.moneylog_backend.moneylog.account.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.moneylog_backend.moneylog.account.dto.AccountDto;

@Mapper
public interface AccountMapper {
    int checkAccountNumber(String accountNumber);

    List<AccountDto> getAccountsByUserId(int userId);
}