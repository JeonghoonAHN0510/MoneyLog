package com.moneylog_backend.moneylog.ledger.service;

import java.util.List;

import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.category.mapper.CategoryMapper;
import com.moneylog_backend.moneylog.ledger.dto.LedgerDto;
import com.moneylog_backend.moneylog.ledger.entity.LedgerEntity;
import com.moneylog_backend.moneylog.ledger.mapper.LedgerMapper;
import com.moneylog_backend.moneylog.ledger.repository.LedgerRepository;
import com.moneylog_backend.moneylog.payment.repository.PaymentRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LedgerService {
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;
    private final CategoryMapper categoryMapper;
    private final LedgerMapper ledgerMapper;

    @Transactional
    public int saveLedger (LedgerDto ledgerDto) {
        AccountEntity accountEntity = accountRepository.findById(ledgerDto.getAccount_id())
                                                       .orElseThrow(
                                                           () -> new IllegalArgumentException("존재하지 않는 계좌입니다."));
        if (!accountEntity.getUser_id().equals(ledgerDto.getUser_id())) {
            throw new AccessDeniedException("본인의 계좌가 아닙니다.");
        }

        String type = categoryMapper.getCategoryTypeByCategoryId(ledgerDto.getCategory_id());
        if (type == null) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다.");
        }

        if ("EXPENSE".equals(type)) {
            if (!paymentRepository.existsById(ledgerDto.getPayment_id())) {
                throw new IllegalArgumentException("유효하지 않은 결제 수단입니다.");
            }
            if (accountEntity.getBalance() < ledgerDto.getAmount()) {
                throw new IllegalArgumentException("잔액이 부족합니다.");
            }
            accountEntity.withdraw(ledgerDto.getAmount());
        } else if ("INCOME".equals(type)) {
            accountEntity.deposit(ledgerDto.getAmount());
        }

        LedgerEntity ledgerEntity = ledgerDto.toEntity();
        ledgerRepository.save(ledgerEntity);

        return ledgerEntity.getLedger_id();
    }

    public List<LedgerDto> getLedgersByUserId (int user_id) {
        return ledgerMapper.getLedgersByUserId(user_id);
    }

    @Transactional
    public boolean deleteLedger (LedgerDto ledgerDto) {
        LedgerEntity ledgerEntity = ledgerRepository.findById(ledgerDto.getLedger_id())
                                                    .orElseThrow(
                                                        () -> new IllegalArgumentException("유효하지 않은 지출 내역입니다."));
        if (!ledgerEntity.getUser_id().equals(ledgerDto.getUser_id())) {
            throw new AccessDeniedException("본인의 지출 내역이 아닙니다.");
        }

        ledgerRepository.delete(ledgerEntity);
        return true;
    }
}