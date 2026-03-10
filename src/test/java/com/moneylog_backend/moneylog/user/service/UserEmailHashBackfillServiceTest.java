package com.moneylog_backend.moneylog.user.service;

import com.moneylog_backend.global.security.pii.PiiCryptoService;
import com.moneylog_backend.moneylog.user.entity.UserEntity;
import com.moneylog_backend.moneylog.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEmailHashBackfillServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PiiCryptoService piiCryptoService;

    @InjectMocks
    private UserEmailHashBackfillService userEmailHashBackfillService;

    @Test
    void email_hash가_null인_사용자는_기동시_backfill된다() {
        UserEntity userEntity = UserEntity.builder()
                                          .userId(1)
                                          .email("tester@moneylog.com")
                                          .build();

        when(userRepository.findAllByEmailHashIsNullOrderByUserIdAsc()).thenReturn(List.of(userEntity));
        when(piiCryptoService.normalizeEmail("tester@moneylog.com")).thenReturn("tester@moneylog.com");
        when(piiCryptoService.hashEmail("tester@moneylog.com")).thenReturn("hash-1");
        when(userRepository.existsByEmailHash("hash-1")).thenReturn(false);

        int updatedCount = userEmailHashBackfillService.backfillMissingEmailHashes();

        assertEquals(1, updatedCount);
        assertEquals("hash-1", userEntity.getEmailHash());
    }

    @Test
    void backfill중_기존_hash와_충돌하면_실패한다() {
        UserEntity userEntity = UserEntity.builder()
                                          .userId(1)
                                          .email("tester@moneylog.com")
                                          .build();

        when(userRepository.findAllByEmailHashIsNullOrderByUserIdAsc()).thenReturn(List.of(userEntity));
        when(piiCryptoService.normalizeEmail("tester@moneylog.com")).thenReturn("tester@moneylog.com");
        when(piiCryptoService.hashEmail("tester@moneylog.com")).thenReturn("hash-1");
        when(userRepository.existsByEmailHash("hash-1")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> userEmailHashBackfillService.backfillMissingEmailHashes());
    }

    @Test
    void backfill중_null_hash_행끼리_중복_이메일이면_실패한다() {
        UserEntity firstUser = UserEntity.builder()
                                         .userId(1)
                                         .email("tester@moneylog.com")
                                         .build();
        UserEntity secondUser = UserEntity.builder()
                                          .userId(2)
                                          .email("tester@moneylog.com")
                                          .build();

        when(userRepository.findAllByEmailHashIsNullOrderByUserIdAsc()).thenReturn(List.of(firstUser, secondUser));
        when(piiCryptoService.normalizeEmail("tester@moneylog.com")).thenReturn("tester@moneylog.com");
        when(piiCryptoService.hashEmail("tester@moneylog.com")).thenReturn("hash-1");
        when(userRepository.existsByEmailHash("hash-1")).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> userEmailHashBackfillService.backfillMissingEmailHashes());
    }
}
