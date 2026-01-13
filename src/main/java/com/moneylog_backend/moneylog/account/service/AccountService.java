package com.moneylog_backend.moneylog.account.service;

import java.util.Optional;

import com.moneylog_backend.global.util.BankAccountNumberFormatter;
import com.moneylog_backend.moneylog.account.dto.AccountDto;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.mapper.AccountMapper;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.bank.service.BankService;
import com.moneylog_backend.moneylog.user.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserService userService;
    private final BankService bankService;

    @Transactional
    public int saveAccount (AccountDto accountDto, String login_id) {
        int user_pk = userService.getUserPK(login_id);
        accountDto.setUser_id(user_pk);

        int bank_id = accountDto.getBank_id();
        if (!bankService.isBankValid(bank_id)) {
            return -1;
        }

        if (accountDto.getNickname() == null || accountDto.getNickname().isEmpty()) {
            String nickname = bankService.getBankName(bank_id);
            accountDto.setNickname(nickname);
        }

        accountDto.setAccount_number(getRegexAccountNumber(bank_id, accountDto.getAccount_number()));

        AccountEntity accountEntity = accountDto.toEntity();
        accountRepository.save(accountEntity);

        return accountEntity.getAccount_id();
    }

    public AccountDto getAccount (int account_id, String login_id) {
        int user_pk = userService.getUserPK(login_id);
        Optional<AccountEntity> accountEntityOptional = accountRepository.findById(account_id);
        if (accountEntityOptional.isPresent()) {
            AccountEntity accountEntity = accountEntityOptional.get();
            if (user_pk == accountEntity.getUser_id()) {
                return accountEntity.toDto();
            }
        }

        return null;
    }

    @Transactional
    public AccountDto updateAccount (AccountDto accountDto, String login_id) {
        int account_id = accountDto.getAccount_id();
        int user_pk = userService.getUserPK(login_id);

        Optional<AccountEntity> accountEntityOptional = accountRepository.findById(account_id);
        if (accountEntityOptional.isPresent()) {
            AccountEntity accountEntity = accountEntityOptional.get();
            if (user_pk == accountEntity.getUser_id()) {
                String InputNickname = accountDto.getNickname();
                String InputAccountNumber = accountDto.getAccount_number();
                int InputBalance = accountDto.getBalance();
                if (InputNickname != null) {
                    accountEntity.setNickname(InputNickname);
                }
                if (InputAccountNumber != null) {
                    accountEntity.setAccount_number(InputAccountNumber);
                }
                if (InputBalance > 0) {
                    accountEntity.setBalance(InputBalance);
                }
                return accountEntity.toDto();
            }
        }

        return null;
    }

    @Transactional
    public boolean deleteAccount (int account_id, String login_id) {
        int user_pk = userService.getUserPK(login_id);

        Optional<AccountEntity> accountEntityOptional = accountRepository.findById(account_id);
        if (accountEntityOptional.isPresent()) {
            AccountEntity accountEntity = accountEntityOptional.get();
            if (user_pk == accountEntity.getUser_id()) {
                accountRepository.deleteById(account_id);
                return true;
            }
        }
        return false;
    }

    public String getRegexAccountNumber (int bank_id, String account_number) {
        String bankName = bankService.getBankName(bank_id);

        return BankAccountNumberFormatter.format(bankName, account_number);
    }
}