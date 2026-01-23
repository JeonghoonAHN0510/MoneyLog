package com.moneylog_backend.moneylog.bank.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moneylog_backend.moneylog.bank.dto.BankDto;
import com.moneylog_backend.moneylog.bank.entity.BankEntity;
import com.moneylog_backend.moneylog.bank.mapper.BankMapper;
import com.moneylog_backend.moneylog.bank.repository.BankRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BankService {
    private final BankRepository bankRepository;
    private final BankMapper bankMapper;

    public List<BankDto> getBankList () {
        List<BankEntity> bankEntities = bankRepository.findAll();

        return bankEntities.stream().map(BankEntity::toDto).collect(Collectors.toList());
    }

    public boolean isBankValid (int bank_id) {
        return bankRepository.existsById(bank_id);
    }

    public String getBankName (int bank_id) {
        BankEntity bankEntity = bankRepository.findById(bank_id)
                                              .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 은행입니다."));

        return bankEntity.getName();
    }
}
