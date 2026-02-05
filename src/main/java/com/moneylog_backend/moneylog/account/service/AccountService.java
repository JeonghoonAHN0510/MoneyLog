package com.moneylog_backend.moneylog.account.service;

import java.util.List;

import com.moneylog_backend.global.util.BankAccountNumberFormatter;
import com.moneylog_backend.moneylog.account.dto.AccountDto;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.mapper.AccountMapper;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.account.repository.TransferRepository;
import com.moneylog_backend.moneylog.bank.entity.BankEntity;
import com.moneylog_backend.moneylog.bank.repository.BankRepository;
import com.moneylog_backend.moneylog.transaction.dto.TransferDto;
import com.moneylog_backend.moneylog.transaction.entity.TransferEntity;
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
    public int saveAccount(AccountDto accountDto, int userId) {

        String finalNickname = accountDto.getNickname();
        String finalAccountNumber = accountDto.getAccountNumber();

        if (accountDto.getBankId() != null) {
            int bankId = accountDto.getBankId();

            if (!isBankValid(bankId)) {
                throw new IllegalArgumentException("유효하지 않은 은행 ID입니다.");
            }

            finalAccountNumber = getRegexAccountNumber(bankId, accountDto.getAccountNumber());
            if (accountMapper.checkAccountNumber(finalAccountNumber) > 0) {
                throw new IllegalArgumentException("이미 등록된 계좌번호입니다.");
            }

            if (finalNickname == null || finalNickname.trim().isEmpty()) {
                finalNickname = getBankName(bankId);
            }
        }

        AccountEntity accountEntity = accountDto.toEntity(userId, finalNickname, finalAccountNumber);
        accountRepository.save(accountEntity);

        return accountEntity.getAccountId();
    }

    public AccountDto getAccount (int accountId, int userId) {
        AccountEntity accountEntity = getAccountByIdAndValidateOwnership(accountId, userId);

        return accountEntity.toDto();
    }

    public List<AccountDto> getAccounts (int userId) {
        return accountMapper.getAccountsByUserId(userId);
    }

    @Transactional
    public AccountDto updateAccount(AccountDto accountDto, int userId) {
        AccountEntity accountEntity = getAccountByIdAndValidateOwnership(accountDto.getAccountId(), userId);

        String newAccountNumber = null;
        String accountNumber = accountDto.getAccountNumber();
        Integer bankId = accountDto.getBankId();
        if (accountNumber != null && !accountNumber.isEmpty()) {
            int targetBankId = ( bankId != null) ? bankId : accountEntity.getBankId();
            newAccountNumber = getRegexAccountNumber(targetBankId, accountNumber);
        }

        accountEntity.updateDetails(
            accountDto.getNickname(),
            newAccountNumber,
            accountDto.getBalance(),
            accountDto.getColor()
        );

        return accountEntity.toDto();
    }

    @Transactional
    public boolean deleteAccount (int accountId, int userId) {
        AccountEntity accountEntity = getAccountByIdAndValidateOwnership(accountId, userId);

        UserEntity userEntity = getUserEntityById(userId);
        Integer userAccountId = userEntity.getAccountId();
        if (userAccountId != null && userAccountId.equals(accountId)) {
            userEntity.deleteAccountId();
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

        AccountEntity fromAccountEntity = getAccountByIdAndValidateOwnership(fromAccountId, userId);
        AccountEntity toAccountEntity = getAccountByIdAndValidateOwnership(toAccountId, userId);

        fromAccountEntity.withdraw(transferBalance);
        toAccountEntity.deposit(transferBalance);

        TransferEntity transferEntity = transferDto.toEntity(userId);
        transferRepository.save(transferEntity);

        return true;
    }

    private String getRegexAccountNumber (int bankId, String accountNumber) {
        String bankName = getBankName(bankId);

        return BankAccountNumberFormatter.format(bankName, accountNumber);
    }

    private UserEntity getUserEntityById (int userId) {
        return userRepository.findById(userId)
                             .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    private AccountEntity getAccountByIdAndValidateOwnership (int accountId, int userId) {
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