package com.moneylog_backend.moneylog.ledger.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.moneylog_backend.moneylog.ledger.dto.LedgerDto;
import com.moneylog_backend.moneylog.ledger.dto.query.SelectLedgerByUserIdQuery;

@Mapper
public interface LedgerMapper {
    List<LedgerDto> getLedgersByUserId(SelectLedgerByUserIdQuery selectQuery);
}