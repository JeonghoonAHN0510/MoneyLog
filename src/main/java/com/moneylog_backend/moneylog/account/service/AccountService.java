package com.moneylog_backend.moneylog.account.service;

import java.util.List;

import com.moneylog_backend.global.type.ColorEnum;
import com.moneylog_backend.global.util.BankAccountNumberFormatter;
import com.moneylog_backend.moneylog.account.dto.AccountDto;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.mapper.AccountMapper;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.account.repository.TransferRepository;
import com.moneylog_backend.moneylog.bank.entity.BankEntity;
import com.moneylog_backend.moneylog.bank.repository.BankRepository;
import com.moneylog_backend.moneylog.ledger.dto.TransferDto;
import com.moneylog_backend.moneylog.ledger.entity.TransferEntity;
import com.moneylog_backend.moneylog.user.entity.UserEntity;
import com.moneylog_backend.moneylog.user.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public int saveAccount (AccountDto accountDto, int user_id) {
        accountDto.setUser_id(user_id);

        if (accountDto.getBank_id() != null) {
            int bank_id = accountDto.getBank_id();
            if (!isBankValid(bank_id)) {
                return -1;
            }

            String regexAccountNumber = getRegexAccountNumber(bank_id, accountDto.getAccount_number());
            int countAccountNumber = accountMapper.checkAccountNumber(regexAccountNumber);
            if (countAccountNumber > 0) {
                return -1;
            }

            if (accountDto.getNickname() == null || accountDto.getNickname().isEmpty()) {
                String nickname = getBankName(bank_id);
                accountDto.setNickname(nickname);
            }

            accountDto.setAccount_number(regexAccountNumber);
        }

        AccountEntity accountEntity = accountDto.toEntity();
        accountRepository.save(accountEntity);

        return accountEntity.getAccount_id();
    }

    public AccountDto getAccount (int account_id, int user_id) {
        AccountEntity accountEntity = getAccountEntityById(account_id, user_id);

        return accountEntity.toDto();
    }

    public List<AccountDto> getAccounts (int user_id) {
        return accountMapper.getAccountsByUserId(user_id);
    }

    @Transactional
    public AccountDto updateAccount (AccountDto accountDto, int user_id) {
        AccountEntity accountEntity = getAccountEntityById(accountDto.getAccount_id(), user_id);

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

        ColorEnum InputColor = accountDto.getColor();
        if (InputColor != null) {
            accountEntity.setColor(InputColor);
        }

        return accountEntity.toDto();
    }

    @Transactional
    public boolean deleteAccount (int account_id, int user_id) {
        AccountEntity accountEntity = getAccountEntityById(account_id, user_id);

        UserEntity userEntity = userRepository.findById(user_id)
                                              .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        if (userEntity.getAccount_id().equals(account_id)) {
            userEntity.setAccount_id(null);
        }

        accountRepository.delete(accountEntity);
        return true;
    }

    @Transactional
    public boolean transferAccountBalance (TransferDto transferDto, int user_id) {
        int transferBalance = transferDto.getAmount();
        if (transferBalance < 0) {
            return false;
        }

        int fromAccountId = transferDto.getFrom_account();
        int toAccountId = transferDto.getTo_account();

        AccountEntity fromAccountEntity = getAccountEntityById(fromAccountId, user_id);
        AccountEntity toAccountEntity = getAccountEntityById(toAccountId, user_id);

        fromAccountEntity.withdraw(transferBalance);
        toAccountEntity.deposit(transferBalance);

        transferDto.setUser_id(user_id);
        TransferEntity transferEntity = transferDto.toEntity();
        transferRepository.save(transferEntity);

        return true;
    }

    private String getRegexAccountNumber (int bank_id, String account_number) {
        String bankName = getBankName(bank_id);

        return BankAccountNumberFormatter.format(bankName, account_number);
    }

    private AccountEntity getAccountEntityById (int account_id, int user_id) {
        AccountEntity accountEntity = accountRepository.findById(account_id)
                                                       .orElseThrow(
                                                           () -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

        if (user_id != accountEntity.getUser_id()) {
            throw new AccessDeniedException("본인의 계좌가 아닙니다.");
        }

        return accountEntity;
    }

    private boolean isBankValid (int bank_id) {
        return bankRepository.existsById(bank_id);
    }

    private String getBankName (int bank_id) {
        BankEntity bankEntity = bankRepository.findById(bank_id)
                                              .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 은행입니다."));

        return bankEntity.getName();
    }
}