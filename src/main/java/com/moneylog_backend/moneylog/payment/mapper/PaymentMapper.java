package com.moneylog_backend.moneylog.payment.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.moneylog_backend.moneylog.payment.dto.PaymentDto;

@Mapper
public interface PaymentMapper {
    List<PaymentDto> getPaymentsByUserId(int user_id);
}