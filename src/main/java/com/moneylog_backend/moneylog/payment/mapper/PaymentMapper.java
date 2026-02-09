package com.moneylog_backend.moneylog.payment.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.moneylog_backend.moneylog.payment.dto.res.PaymentResDto;

@Mapper
public interface PaymentMapper {
    List<PaymentResDto> getPaymentsByUserId(int userId);
}