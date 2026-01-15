package com.moneylog_backend.moneylog.payment.service;

import java.util.List;

import com.moneylog_backend.moneylog.payment.dto.PaymentDto;
import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;
import com.moneylog_backend.moneylog.payment.mapper.PaymentMapper;
import com.moneylog_backend.moneylog.payment.repository.PaymentRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public int savePayment(PaymentDto paymentDto, int user_id) {
        paymentDto.setUser_id(user_id);

        PaymentEntity paymentEntity = paymentDto.toEntity();
        paymentRepository.save(paymentEntity);

        return paymentEntity.getPayment_id();
    }

    public List<PaymentDto> getPaymentsByUserId(int user_id) {
        return paymentMapper.getPaymentsByUserId(user_id);
    }
}