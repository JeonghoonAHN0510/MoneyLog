package com.moneylog_backend.moneylog.payment.service;

import java.util.List;

import com.moneylog_backend.global.type.PaymentEnum;
import com.moneylog_backend.moneylog.payment.dto.PaymentDto;
import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;
import com.moneylog_backend.moneylog.payment.mapper.PaymentMapper;
import com.moneylog_backend.moneylog.payment.repository.PaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public int savePayment (PaymentDto paymentDto, int user_id) {
        paymentDto.setUser_id(user_id);

        PaymentEntity paymentEntity = paymentDto.toEntity();
        paymentRepository.save(paymentEntity);

        return paymentEntity.getPayment_id();
    }

    public List<PaymentDto> getPaymentsByUserId (int user_id) {
        return paymentMapper.getPaymentsByUserId(user_id);
    }

    @Transactional
    public PaymentDto updatePayment (PaymentDto paymentDto, int user_id) {
        PaymentEntity paymentEntity = paymentRepository.findById(paymentDto.getPayment_id())
                                                       .orElseThrow(
                                                           () -> new IllegalArgumentException("존재하지 않는 결제수단입니다."));

        if (paymentEntity.getUser_id() != user_id) {
            return null;
        }

        String InputName = paymentDto.getName();
        PaymentEnum InputType = paymentDto.getType();
        if (InputName != null) {
            paymentEntity.setName(InputName);
        }
        if (InputType != null) {
            paymentEntity.setType(InputType);
        }

        return paymentEntity.toDto();
    }

    @Transactional
    public boolean deletePayment (int payment_id, int user_id) {
        PaymentEntity paymentEntity = paymentRepository.findById(payment_id)
                                                       .orElseThrow(
                                                           () -> new IllegalArgumentException("존재하지 않는 결제수단입니다."));

        if (paymentEntity.getUser_id() != user_id) {
            return false;
        }

        paymentRepository.delete(paymentEntity);
        return true;
    }
}