package com.moneylog_backend.moneylog.payment.service;

import java.util.List;

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
    public int savePayment (PaymentDto paymentDto, int userId) {
        PaymentEntity paymentEntity = paymentDto.toEntity(userId);
        paymentRepository.save(paymentEntity);

        return paymentEntity.getPaymentId();
    }

    public List<PaymentDto> getPaymentsByUserId (int userId) {
        return paymentMapper.getPaymentsByUserId(userId);
    }

    @Transactional
    public PaymentDto updatePayment (PaymentDto paymentDto, int userId) {
        Integer accountId = paymentDto.getAccountId();
        validateAccountOwnership(accountId, userId);

        PaymentEntity paymentEntity = getPaymentByIdAndValidateOwnership(paymentDto.getPaymentId(), userId);
        paymentEntity.updateDetails(
            accountId,
            paymentDto.getName(),
            paymentDto.getType()
        );

        return paymentEntity.toDto();
    }

    @Transactional
    public boolean deletePayment (int paymentId, int userId) {
        PaymentEntity paymentEntity = getPaymentByIdAndValidateOwnership(paymentId, userId);

        paymentRepository.delete(paymentEntity);
        return true;
    }

    private PaymentEntity getPaymentByIdAndValidateOwnership (int paymentId, int userId) {
        PaymentEntity paymentEntity = paymentRepository.findById(paymentId)
                                                       .orElseThrow(
                                                           () -> new IllegalArgumentException("존재하지 않는 결제수단입니다."));

        if (paymentEntity.getUserId() != userId) {
            throw new AccessDeniedException("본인의 결제수단이 아닙니다.");
        }

        return paymentEntity;
    }

    private void validateAccountOwnership(int accountId, int userId) {
        AccountEntity accountEntity = accountRepository.findById(accountId)
                                                       .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

        if (!accountEntity.getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 계좌가 아닙니다.");
        }
    }
}