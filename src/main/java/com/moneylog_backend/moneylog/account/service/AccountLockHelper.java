package com.moneylog_backend.moneylog.account.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.exception.ResourceNotFoundException;
import com.moneylog_backend.global.util.OwnershipValidator;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountLockHelper {
    private final AccountRepository accountRepository;

    public AccountEntity lockOwnedAccount (Integer accountId, Integer userId) {
        return lockOwnedAccount(accountId, userId, "본인의 계좌가 아닙니다.");
    }

    public AccountEntity lockOwnedAccount (Integer accountId, Integer userId, String ownershipMessage) {
        AccountEntity accountEntity = accountRepository.findByIdForUpdate(accountId)
                                                       .orElseThrow(
                                                           () -> new ResourceNotFoundException(
                                                               ErrorMessageConstants.ACCOUNT_NOT_FOUND));
        OwnershipValidator.validateOwner(accountEntity.getUserId(), userId, ownershipMessage);
        return accountEntity;
    }

    public Map<Integer, AccountEntity> lockOwnedAccounts (List<Integer> accountIds, Integer userId) {
        return lockOwnedAccounts(accountIds, userId, "본인의 계좌가 아닙니다.");
    }

    public Map<Integer, AccountEntity> lockOwnedAccounts (List<Integer> accountIds,
                                                          Integer userId,
                                                          String ownershipMessage) {
        List<Integer> normalizedIds = accountIds.stream()
                                                .distinct()
                                                .sorted()
                                                .toList();
        if (normalizedIds.isEmpty()) {
            return Map.of();
        }

        List<AccountEntity> lockedAccounts = accountRepository.findAllByAccountIdInOrderByAccountIdAscForUpdate(normalizedIds);
        Map<Integer, AccountEntity> accountById = new LinkedHashMap<>();
        for (AccountEntity accountEntity : lockedAccounts) {
            OwnershipValidator.validateOwner(accountEntity.getUserId(), userId, ownershipMessage);
            accountById.put(accountEntity.getAccountId(), accountEntity);
        }

        for (Integer accountId : normalizedIds) {
            if (!accountById.containsKey(accountId)) {
                throw new ResourceNotFoundException(ErrorMessageConstants.ACCOUNT_NOT_FOUND);
            }
        }

        return accountById;
    }
}
