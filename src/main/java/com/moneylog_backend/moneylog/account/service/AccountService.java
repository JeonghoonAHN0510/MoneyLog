package com.moneylog_backend.moneylog.account.service;

import java.util.List;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.exception.ResourceNotFoundException;
import com.moneylog_backend.global.util.BankAccountNumberFormatter;
import com.moneylog_backend.global.util.InputStringNormalizer;
import com.moneylog_backend.global.util.OwnershipValidator;
import com.moneylog_backend.moneylog.account.dto.req.AccountReqDto;
import com.moneylog_backend.moneylog.account.dto.res.AccountResDto;
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

import org.springframework.dao.DataIntegrityViolationException;
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
    public int saveAccount(AccountReqDto accountReqDto, int userId) {
        String finalNickname = InputStringNormalizer.trimToNull(accountReqDto.getNickname());
        String finalAccountNumber = InputStringNormalizer.trimToNull(accountReqDto.getAccountNumber());

        if (accountReqDto.getBankId() != null) {
            int bankId = accountReqDto.getBankId();

            if (!isBankValid(bankId)) {
                throw new IllegalArgumentException("유효하지 않은 은행 ID입니다.");
            }

            finalAccountNumber = finalAccountNumber == null ? null : getRegexAccountNumber(bankId, finalAccountNumber);
            if (finalAccountNumber != null && accountMapper.checkAccountNumber(finalAccountNumber) > 0) {
                throw new IllegalArgumentException("이미 등록된 계좌번호입니다.");
            }

            if (finalNickname == null || finalNickname.trim().isEmpty()) {
                finalNickname = getBankName(bankId);
            }
        }

        AccountEntity accountEntity = accountReqDto.toEntity(userId, finalNickname, finalAccountNumber);
        try {
            accountRepository.saveAndFlush(accountEntity);
        } catch (DataIntegrityViolationException ex) {
            throw duplicateAccountNumberException();
        }

        return accountEntity.getAccountId();
    }

    public AccountResDto getAccount(int accountId, int userId) {
        AccountEntity accountEntity = getAccountByIdAndValidateOwnership(accountId, userId);

        return accountEntity.toDto();
    }

    public List<AccountResDto> getAccounts(int userId) {
        return accountMapper.getAccountsByUserId(userId);
    }

    @Transactional
    public AccountResDto updateAccount(AccountReqDto accountReqDto, int userId) {
        AccountEntity accountEntity = lockAccountByIdAndValidateOwnership(accountReqDto.getAccountId(), userId);

        String newAccountNumber = null;
        String accountNumber = InputStringNormalizer.trimToNull(accountReqDto.getAccountNumber());
        String normalizedNickname = InputStringNormalizer.trimToNull(accountReqDto.getNickname());
        Integer bankId = accountReqDto.getBankId();
        if (accountNumber != null) {
            int targetBankId = resolveAccountNumberBankId(bankId, accountEntity.getBankId());
            newAccountNumber = getRegexAccountNumber(targetBankId, accountNumber);
        }

        accountEntity.updateDetails(normalizedNickname, newAccountNumber, accountReqDto.getBalance(),
                                    accountReqDto.getColor());
        try {
            accountRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw duplicateAccountNumberException();
        }

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

        List<AccountEntity> lockedAccounts = lockAccountsAndValidateOwnership(List.of(fromAccountId, toAccountId), userId);
        AccountEntity fromAccountEntity = findLockedAccount(lockedAccounts, fromAccountId);
        AccountEntity toAccountEntity = findLockedAccount(lockedAccounts, toAccountId);

        fromAccountEntity.withdraw(transferBalance);
        toAccountEntity.deposit(transferBalance);

        String normalizedMemo = InputStringNormalizer.trimNullable(transferDto.getMemo());
        TransferEntity transferEntity = transferDto.toEntity(userId, normalizedMemo);
        transferRepository.save(transferEntity);

        return true;
    }

    private String getRegexAccountNumber (int bankId, String accountNumber) {
        String bankName = getBankName(bankId);

        return BankAccountNumberFormatter.format(bankName, accountNumber);
    }

    private UserEntity getUserEntityById (int userId) {
        return userRepository.findById(userId)
                             .orElseThrow(() -> new ResourceNotFoundException(ErrorMessageConstants.USER_NOT_FOUND));
    }

    private AccountEntity getAccountByIdAndValidateOwnership (int accountId, int userId) {
        AccountEntity accountEntity = accountRepository.findById(accountId)
                                                       .orElseThrow(
                                                           () -> new ResourceNotFoundException(
                                                               ErrorMessageConstants.ACCOUNT_NOT_FOUND));

        OwnershipValidator.validateOwner(accountEntity.getUserId(), userId, "본인의 계좌가 아닙니다.");

        return accountEntity;
    }

    private AccountEntity lockAccountByIdAndValidateOwnership (int accountId, int userId) {
        AccountEntity accountEntity = accountRepository.findByIdForUpdate(accountId)
                                                       .orElseThrow(
                                                           () -> new ResourceNotFoundException(
                                                               ErrorMessageConstants.ACCOUNT_NOT_FOUND));

        OwnershipValidator.validateOwner(accountEntity.getUserId(), userId, "본인의 계좌가 아닙니다.");

        return accountEntity;
    }

    private List<AccountEntity> lockAccountsAndValidateOwnership (List<Integer> accountIds, int userId) {
        List<Integer> sortedDistinctAccountIds = accountIds.stream().distinct().sorted().toList();
        List<AccountEntity> lockedAccounts = accountRepository.findAllByAccountIdInForUpdate(sortedDistinctAccountIds);
        if (lockedAccounts.size() != sortedDistinctAccountIds.size()) {
            throw new ResourceNotFoundException(ErrorMessageConstants.ACCOUNT_NOT_FOUND);
        }

        lockedAccounts.forEach(account ->
            OwnershipValidator.validateOwner(account.getUserId(), userId, "본인의 계좌가 아닙니다.")
        );
        return lockedAccounts;
    }

    private AccountEntity findLockedAccount (List<AccountEntity> lockedAccounts, int accountId) {
        return lockedAccounts.stream()
                             .filter(account -> Integer.valueOf(accountId).equals(account.getAccountId()))
                             .findFirst()
                             .orElseThrow(() -> new ResourceNotFoundException(ErrorMessageConstants.ACCOUNT_NOT_FOUND));
    }

    private IllegalArgumentException duplicateAccountNumberException () {
        return new IllegalArgumentException("이미 등록된 계좌번호입니다.");
    }

    private int resolveAccountNumberBankId (Integer requestBankId, Integer currentBankId) {
        Integer targetBankId = requestBankId != null ? requestBankId : currentBankId;
        if (targetBankId == null) {
            throw new IllegalArgumentException("계좌번호를 등록하려면 은행 ID가 필요합니다.");
        }
        if (!isBankValid(targetBankId)) {
            throw new IllegalArgumentException("유효하지 않은 은행 ID입니다.");
        }

        return targetBankId;
    }

    private boolean isBankValid (int bankId) {
        return bankRepository.existsById(bankId);
    }

    private String getBankName (int bankId) {
        BankEntity bankEntity = bankRepository.findById(bankId)
                                              .orElseThrow(
                                                  () -> new ResourceNotFoundException(ErrorMessageConstants.BANK_NOT_FOUND));

        return bankEntity.getName();
    }
}
