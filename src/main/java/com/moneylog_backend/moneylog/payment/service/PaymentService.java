package com.moneylog_backend.moneylog.payment.service;

import java.util.List;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.exception.ResourceNotFoundException;
import com.moneylog_backend.global.type.PaymentEnum;
import com.moneylog_backend.global.util.OwnershipValidator;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.payment.dto.req.PaymentReqDto;
import com.moneylog_backend.moneylog.payment.dto.res.PaymentResDto;
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
    private final AccountRepository accountRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public int savePayment(PaymentReqDto paymentReqDto, int userId) {
        Integer accountId = resolveAccountId(paymentReqDto.getType(), paymentReqDto.getAccountId(), userId);

        PaymentEntity paymentEntity = PaymentEntity.builder()
                                                   .userId(userId)
                                                   .accountId(accountId)
                                                   .name(paymentReqDto.getName())
                                                   .type(paymentReqDto.getType())
                                                   .build();
        paymentRepository.save(paymentEntity);

        return paymentEntity.getPaymentId();
    }

    public List<PaymentResDto> getPaymentsByUserId(int userId) {
        return paymentMapper.getPaymentsByUserId(userId);
    }

    @Transactional
    public PaymentResDto updatePayment(PaymentReqDto paymentReqDto, int userId) {
        Integer accountId = resolveAccountId(paymentReqDto.getType(), paymentReqDto.getAccountId(), userId);

        PaymentEntity paymentEntity = getPaymentByIdAndValidateOwnership(paymentReqDto.getPaymentId(), userId);
        paymentEntity.updateDetails(accountId, paymentReqDto.getName(), paymentReqDto.getType());

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
                                                           () -> new ResourceNotFoundException(
                                                               ErrorMessageConstants.PAYMENT_NOT_FOUND));

        OwnershipValidator.validateOwner(paymentEntity.getUserId(), userId, "본인의 결제수단이 아닙니다.");

        return paymentEntity;
    }

    private void validateAccountOwnership (Integer accountId, int userId) {
        AccountEntity accountEntity = accountRepository.findById(accountId)
                                                       .orElseThrow(
                                                           () -> new ResourceNotFoundException(
                                                               ErrorMessageConstants.ACCOUNT_NOT_FOUND));

        OwnershipValidator.validateOwner(accountEntity.getUserId(), userId, "본인의 계좌가 아닙니다.");
    }

    private Integer resolveAccountId (PaymentEnum type, Integer accountId, int userId) {
        if (PaymentEnum.CASH.equals(type)) {
            return null;
        }

        if (accountId == null) {
            throw new IllegalArgumentException("계좌 ID는 필수입니다.");
        }

        validateAccountOwnership(accountId, userId);
        return accountId;
    }
}
