package com.moneylog_backend.moneylog.bank.service;

import java.util.List;
import java.util.Optional;
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
        Optional<BankEntity> optionalBankEntity = bankRepository.findById(bank_id);
        if (optionalBankEntity.isPresent()) {
            BankEntity bankEntity = optionalBankEntity.get();
            return bankEntity.getName();
        }
        return null;
    }
}
