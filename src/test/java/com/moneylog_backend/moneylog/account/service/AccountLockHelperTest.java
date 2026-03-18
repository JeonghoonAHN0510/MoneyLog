package com.moneylog_backend.moneylog.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.moneylog_backend.global.type.AccountTypeEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;

class AccountLockHelperTest {
    private AccountRepository accountRepository;
    private AccountLockHelper accountLockHelper;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        accountLockHelper = new AccountLockHelper(accountRepository);
    }

    @Test
    void 여러_계좌_잠금은_ID_오름차순으로_정렬하고_중복을_제거한다() {
        AccountEntity first = account(1, 7, 1000);
        AccountEntity third = account(3, 7, 2000);
        when(accountRepository.findAllByAccountIdInOrderByAccountIdAscForUpdate(anyList()))
            .thenReturn(List.of(first, third));

        Map<Integer, AccountEntity> lockedAccounts = accountLockHelper.lockOwnedAccounts(List.of(3, 1, 3), 7);

        ArgumentCaptor<List<Integer>> captor = ArgumentCaptor.forClass(List.class);
        verify(accountRepository).findAllByAccountIdInOrderByAccountIdAscForUpdate(captor.capture());
        assertEquals(List.of(1, 3), captor.getValue());
        assertEquals(List.of(1, 3), lockedAccounts.keySet().stream().toList());
    }

    @Test
    void 단일_계좌_잠금은_for_update_조회로_가져온다() {
        AccountEntity accountEntity = account(10, 7, 1000);
        when(accountRepository.findByIdForUpdate(10)).thenReturn(java.util.Optional.of(accountEntity));

        AccountEntity lockedAccount = accountLockHelper.lockOwnedAccount(10, 7);

        verify(accountRepository).findByIdForUpdate(10);
        assertEquals(10, lockedAccount.getAccountId());
    }

    private AccountEntity account(int accountId, int userId, int balance) {
        return AccountEntity.builder()
                            .accountId(accountId)
                            .userId(userId)
                            .balance(balance)
                            .type(AccountTypeEnum.BANK)
                            .nickname("locked")
                            .build();
    }
}
