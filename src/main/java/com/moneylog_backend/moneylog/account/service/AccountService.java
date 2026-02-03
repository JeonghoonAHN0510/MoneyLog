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
    public int saveAccount (AccountDto accountDto, int userId) {
        accountDto.setUserId(userId);

        if (accountDto.getBankId() != null) {
            int bankId = accountDto.getBankId();
            if (!isBankValid(bankId)) {
                return -1;
            }

            String regexAccountNumber = getRegexAccountNumber(bankId, accountDto.getAccountNumber());
            int countAccountNumber = accountMapper.checkAccountNumber(regexAccountNumber);
            if (countAccountNumber > 0) {
                return -1;
            }

            if (accountDto.getNickname() == null || accountDto.getNickname().isEmpty()) {
                String nickname = getBankName(bankId);
                accountDto.setNickname(nickname);
            }

            accountDto.setAccountNumber(regexAccountNumber);
        }

        AccountEntity accountEntity = accountDto.toEntity();
        accountRepository.save(accountEntity);

        return accountEntity.getAccountId();
    }

    public AccountDto getAccount (int accountId, int userId) {
        AccountEntity accountEntity = getAccountEntityById(accountId, userId);

        return accountEntity.toDto();
    }

    public List<AccountDto> getAccounts (int userId) {
        return accountMapper.getAccountsByUserId(userId);
    }

    @Transactional
    public AccountDto updateAccount (AccountDto accountDto, int userId) {
        AccountEntity accountEntity = getAccountEntityById(accountDto.getAccountId(), userId);

        String InputNickname = accountDto.getNickname();
        if (InputNickname != null) {
            accountEntity.setNickname(InputNickname);
        }

        String InputAccountNumber = accountDto.getAccountNumber();
        if (InputAccountNumber != null) {
            accountEntity.setAccountNumber(InputAccountNumber);
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
    public boolean deleteAccount (int accountId, int userId) {
        AccountEntity accountEntity = getAccountEntityById(accountId, userId);

        UserEntity userEntity = userRepository.findById(userId)
                                              .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Integer userAccountId = userEntity.getAccountId();
        if (userAccountId != null && userAccountId.equals(accountId)) {
            userEntity.setAccountId(null);
        }

        accountRepository.delete(accountEntity);
        return true;
    }

    @Transactional
    public boolean transferAccountBalance (TransferDto transferDto, int userId) {
        int transferBalance = transferDto.getAmount();
        if (transferBalance < 0) {
            return false;
        }

        int fromAccountId = transferDto.getFromAccount();
        int toAccountId = transferDto.getToAccount();

        AccountEntity fromAccountEntity = getAccountEntityById(fromAccountId, userId);
        AccountEntity toAccountEntity = getAccountEntityById(toAccountId, userId);

        fromAccountEntity.withdraw(transferBalance);
        toAccountEntity.deposit(transferBalance);

        transferDto.setUserId(userId);
        TransferEntity transferEntity = transferDto.toEntity();
        transferRepository.save(transferEntity);

        return true;
    }

    private String getRegexAccountNumber (int bankId, String accountNumber) {
        String bankName = getBankName(bankId);

        return BankAccountNumberFormatter.format(bankName, accountNumber);
    }

    private AccountEntity getAccountEntityById (int accountId, int userId) {
        AccountEntity accountEntity = accountRepository.findById(accountId)
                                                       .orElseThrow(
                                                           () -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

        if (userId != accountEntity.getUserId()) {
            throw new AccessDeniedException("본인의 계좌가 아닙니다.");
        }

        return accountEntity;
    }

    private boolean isBankValid (int bankId) {
        return bankRepository.existsById(bankId);
    }

    private String getBankName (int bankId) {
        BankEntity bankEntity = bankRepository.findById(bankId)
                                              .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 은행입니다."));

        return bankEntity.getName();
    }
}