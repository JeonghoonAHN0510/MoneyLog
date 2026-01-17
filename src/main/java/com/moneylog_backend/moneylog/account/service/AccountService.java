package com.moneylog_backend.moneylog.account.service;

import java.util.List;

import com.moneylog_backend.global.util.BankAccountNumberFormatter;
import com.moneylog_backend.moneylog.account.dto.AccountDto;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.mapper.AccountMapper;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.bank.service.BankService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final BankService bankService;

    @Transactional
    public int saveAccount (AccountDto accountDto, int user_id) {
        accountDto.setUser_id(user_id);

        int bank_id = accountDto.getBank_id();
        if (!bankService.isBankValid(bank_id)) {
            return -1;
        }

        String regexAccountNumber = getRegexAccountNumber(bank_id, accountDto.getAccount_number());
        int countAccountNumber = accountMapper.checkAccountNumber(regexAccountNumber);
        if (countAccountNumber > 0) {
            return -1;
        }

        if (accountDto.getNickname() == null || accountDto.getNickname().isEmpty()) {
            String nickname = bankService.getBankName(bank_id);
            accountDto.setNickname(nickname);
        }

        accountDto.setAccount_number(regexAccountNumber);

        AccountEntity accountEntity = accountDto.toEntity();
        accountRepository.save(accountEntity);

        return accountEntity.getAccount_id();
    }

    public AccountDto getAccount (int account_id, int user_id) {
        AccountEntity accountEntity = accountRepository.findById(account_id).orElse(null);
        if (accountEntity == null) {
            return null;
        }

        if (user_id != accountEntity.getUser_id()) {
            return null;
        }

        return accountEntity.toDto();
    }

    public List<AccountDto> getAccounts (int user_id) {
        return accountMapper.getAccountsByUserId(user_id);
    }

    @Transactional
    public AccountDto updateAccount (AccountDto accountDto, int user_id) {
        AccountEntity accountEntity = accountRepository.findById(accountDto.getAccount_id()).orElse(null);
        if (accountEntity == null) {
            return null;
        }

        if (user_id != accountEntity.getUser_id()) {
            return null;
        }

        String InputNickname = accountDto.getNickname();
        if (InputNickname != null) {
            accountEntity.setNickname(InputNickname);
        }

        String InputAccountNumber = accountDto.getAccount_number();
        if (InputAccountNumber != null) {
            accountEntity.setAccount_number(InputAccountNumber);
        }

        int InputBalance = accountDto.getBalance();
        if (InputBalance > 0) {
            accountEntity.setBalance(InputBalance);
        }

        return accountEntity.toDto();
    }

    @Transactional
    public boolean deleteAccount (int account_id, int user_id) {
        AccountEntity accountEntity = accountRepository.findById(account_id).orElse(null);
        if (accountEntity == null) {
            return false;
        }
        if (user_id != accountEntity.getUser_id()) {
            return false;
        }

        accountRepository.delete(accountEntity);
        return true;
    }

    @Transactional
    public boolean transferAccountBalance (AccountDto accountDto, int user_id) {
        int transferBalance = accountDto.getBalance();
        if (transferBalance < 0) {
            return false;
        }

        AccountEntity fromAccountEntity = accountRepository.findById(accountDto.getFrom_account_id()).orElse(null);
        AccountEntity toAccountEntity = accountRepository.findById(accountDto.getTo_account_id()).orElse(null);
        if (fromAccountEntity == null || toAccountEntity == null) {
            return false;
        }

        int from_balance = fromAccountEntity.getBalance();
        int to_balance = toAccountEntity.getBalance();
        if (user_id != toAccountEntity.getUser_id() || user_id != fromAccountEntity.getUser_id()) {
            return false;
        }
        if (from_balance < transferBalance) {
            return false;
        }

        fromAccountEntity.setBalance(from_balance - transferBalance);
        toAccountEntity.setBalance(to_balance + transferBalance);

        return true;
    }

    public String getRegexAccountNumber (int bank_id, String account_number) {
        String bankName = bankService.getBankName(bank_id);

        return BankAccountNumberFormatter.format(bankName, account_number);
    }
}