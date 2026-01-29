package com.moneylog_backend.moneylog.payment.service;

import java.util.List;

import com.moneylog_backend.global.type.PaymentEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.payment.dto.PaymentDto;
import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;
import com.moneylog_backend.moneylog.payment.mapper.PaymentMapper;
import com.moneylog_backend.moneylog.payment.repository.PaymentRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
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
        Integer account_id = paymentDto.getAccount_id();
        validateAccountOwnership(account_id, user_id);

        PaymentEntity paymentEntity = getPaymentEntityById(paymentDto.getPayment_id(), user_id);
        paymentEntity.setAccount_id(account_id);

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
        PaymentEntity paymentEntity = getPaymentEntityById(payment_id, user_id);

        paymentRepository.delete(paymentEntity);
        return true;
    }

    private PaymentEntity getPaymentEntityById (int payment_id, int user_id) {
        PaymentEntity paymentEntity = paymentRepository.findById(payment_id)
                                                       .orElseThrow(
                                                           () -> new IllegalArgumentException("존재하지 않는 결제수단입니다."));

        if (paymentEntity.getUser_id() != user_id) {
            throw new AccessDeniedException("본인의 결제수단이 아닙니다.");
        }

        return paymentEntity;
    }

    private void validateAccountOwnership(int accountId, int userId) {
        AccountEntity accountEntity = accountRepository.findById(accountId)
                                                       .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

        if (!accountEntity.getUser_id().equals(userId)) {
            throw new AccessDeniedException("본인의 계좌가 아닙니다.");
        }
    }
}