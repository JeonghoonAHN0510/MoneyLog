package com.moneylog_backend.moneylog.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.moneylog_backend.global.type.AccountTypeEnum;
import com.moneylog_backend.global.type.ColorEnum;
import com.moneylog_backend.moneylog.account.dto.req.AccountReqDto;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.mapper.AccountMapper;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.account.repository.TransferRepository;
import com.moneylog_backend.moneylog.bank.repository.BankRepository;
import com.moneylog_backend.moneylog.transaction.dto.TransferDto;
import com.moneylog_backend.moneylog.user.repository.UserRepository;

class AccountServiceLockingTest {
    private AccountLockHelper accountLockHelper;
    private TransferRepository transferRepository;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountLockHelper = mock(AccountLockHelper.class);
        transferRepository = mock(TransferRepository.class);
        accountService = new AccountService(
            mock(AccountRepository.class),
            accountLockHelper,
            transferRepository,
            mock(UserRepository.class),
            mock(BankRepository.class),
            mock(AccountMapper.class)
        );
    }

    @Test
    void 수동_잔액_수정은_잠금헬퍼를_거친다() {
        AccountEntity accountEntity = account(10, 7, 1000);
        when(accountLockHelper.lockOwnedAccount(10, 7)).thenReturn(accountEntity);

        AccountReqDto request = AccountReqDto.builder()
                                             .accountId(10)
                                             .nickname("생활비")
                                             .balance(2500)
                                             .type(AccountTypeEnum.BANK)
                                             .color(ColorEnum.BLUE)
                                             .build();

        accountService.updateAccount(request, 7);

        verify(accountLockHelper).lockOwnedAccount(10, 7);
        assertEquals(2500, accountEntity.getBalance());
        assertEquals("생활비", accountEntity.getNickname());
    }

    @Test
    void 계좌_이체는_잠금헬퍼로_두_계좌를_함께_조회한다() {
        AccountEntity fromAccount = account(30, 7, 5000);
        AccountEntity toAccount = account(10, 7, 1000);
        when(accountLockHelper.lockOwnedAccounts(List.of(30, 10), 7))
            .thenReturn(Map.of(30, fromAccount, 10, toAccount));

        TransferDto transferDto = TransferDto.builder()
                                             .fromAccount(30)
                                             .toAccount(10)
                                             .amount(700)
                                             .transferAt(LocalDate.of(2026, 3, 18))
                                             .memo("월급 통합")
                                             .build();

        accountService.transferAccountBalance(transferDto, 7);

        verify(accountLockHelper).lockOwnedAccounts(List.of(30, 10), 7);
        verify(transferRepository).save(any());
        assertEquals(4300, fromAccount.getBalance());
        assertEquals(1700, toAccount.getBalance());
    }

    private AccountEntity account(int accountId, int userId, int balance) {
        return AccountEntity.builder()
                            .accountId(accountId)
                            .userId(userId)
                            .balance(balance)
                            .nickname("locked")
                            .type(AccountTypeEnum.BANK)
                            .build();
    }
}
