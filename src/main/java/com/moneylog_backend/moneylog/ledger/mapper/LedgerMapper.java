package com.moneylog_backend.moneylog.ledger.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.moneylog_backend.moneylog.ledger.dto.LedgerDto;

@Mapper
public interface LedgerMapper {
    List<LedgerDto> getLedgersByUserId(int user_id);
}