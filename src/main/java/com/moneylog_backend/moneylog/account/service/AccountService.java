package com.moneylog_backend.moneylog.account.service;

import java.util.Optional;

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
        int user_id = accountDto.getUser_id();
        String user_login_id = userService.getUserId(user_id);
        if (!user_login_id.equals(login_id)) {
            return -1;
        }

        int bank_id = accountDto.getBank_id();
        if (!bankService.isBankValid(bank_id)) {
            return -1;
        }

        if (accountDto.getNickname() == null || accountDto.getNickname().isEmpty()) {
            String nickname = bankService.getBankName(bank_id);
            accountDto.setNickname(nickname);
        }
        AccountEntity accountEntity = accountDto.toEntity();
        accountRepository.save(accountEntity);

        return accountEntity.getAccount_id();
    }

    public AccountDto getAccount (int account_id, String login_id) {
        int user_id = userService.getUserPK(login_id);
        Optional<AccountEntity> accountEntityOptional = accountRepository.findById(account_id);
        if (accountEntityOptional.isPresent()) {
            AccountEntity accountEntity = accountEntityOptional.get();
            if (user_id == accountEntity.getUser_id()) {
                return accountEntity.toDto();
            }
        }

        return null;
    }

    @Transactional
    public AccountDto updateAccount (AccountDto accountDto, String login_id) {
        int account_id = accountDto.getAccount_id();
        int user_id = userService.getUserPK(login_id);

        Optional<AccountEntity> accountEntityOptional = accountRepository.findById(account_id);
        if (accountEntityOptional.isPresent()) {
            AccountEntity accountEntity = accountEntityOptional.get();
            if (user_id == accountEntity.getUser_id()) {
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
}