package com.moneylog_backend.moneylog.user.service;

import com.moneylog_backend.global.auth.jwt.JwtProvider;
import com.moneylog_backend.global.file.FileStorageService;
import com.moneylog_backend.global.file.cleanup.FileDeleteTaskService;
import com.moneylog_backend.global.util.FormatUtils;
import com.moneylog_backend.global.util.RedisService;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.bank.entity.BankEntity;
import com.moneylog_backend.moneylog.bank.repository.BankRepository;
import com.moneylog_backend.moneylog.user.dto.UserDto;
import com.moneylog_backend.moneylog.user.mapper.UserMapper;
import com.moneylog_backend.moneylog.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BankRepository bankRepository;
    @Mock
    private RedisService redisService;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private FormatUtils formatUtils;
    @Mock
    private UserMapper userMapper;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private UserWriteTxService userWriteTxService;
    @Mock
    private FileDeleteTaskService fileDeleteTaskService;

    @InjectMocks
    private UserService userService;

    @Test
    void 프로필이미지_변경_성공시_기존파일은_삭제큐에_적재한다() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "profile.jpg",
            "image/jpeg",
            "abc".getBytes(StandardCharsets.UTF_8)
        );
        UserDto responseDto = UserDto.builder().id("tester").build();

        when(fileStorageService.storeFile(eq(file), eq("profile"))).thenReturn("/uploads/new.jpg");
        when(userWriteTxService.updateProfileImageUrl("tester", "/uploads/new.jpg"))
            .thenReturn(new ProfileImageUpdateTxResult(responseDto, "/uploads/old.jpg"));

        UserDto result = userService.updateProfileImage("tester", file);

        assertEquals(responseDto, result);
        verify(fileDeleteTaskService).enqueueDelete("/uploads/old.jpg", "PROFILE_IMAGE_REPLACED");
        verify(fileDeleteTaskService, never()).deleteNowOrEnqueue(eq("/uploads/new.jpg"), any());
    }

    @Test
    void 프로필이미지_트랜잭션_실패시_신규파일_보상삭제를_시도한다() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "profile.jpg",
            "image/jpeg",
            "abc".getBytes(StandardCharsets.UTF_8)
        );

        when(fileStorageService.storeFile(eq(file), eq("profile"))).thenReturn("/uploads/new.jpg");
        when(userWriteTxService.updateProfileImageUrl("tester", "/uploads/new.jpg"))
            .thenThrow(new RuntimeException("db fail"));

        assertThrows(RuntimeException.class, () -> userService.updateProfileImage("tester", file));
        verify(fileDeleteTaskService).deleteNowOrEnqueue("/uploads/new.jpg", "PROFILE_IMAGE_TX_ROLLBACK");
        verify(fileDeleteTaskService, never()).enqueueDelete(any(), any());
    }

    @Test
    void 회원가입_트랜잭션_실패시_신규파일_보상삭제를_시도한다() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "profile.jpg",
            "image/jpeg",
            "abc".getBytes(StandardCharsets.UTF_8)
        );
        UserDto request = UserDto.builder()
                                 .id("tester")
                                 .email("tester@moneylog.com")
                                 .phone("01012341234")
                                 .password("password")
                                 .bankId(1)
                                 .bankName("한국은행")
                                 .accountNumber("123123123123")
                                 .uploadFile(file)
                                 .build();

        when(userRepository.existsByLoginId("tester")).thenReturn(false);
        when(userRepository.existsByEmail("tester@moneylog.com")).thenReturn(false);
        when(bankRepository.findById(1)).thenReturn(Optional.of(BankEntity.builder().bankId(1).name("한국은행").code("001").build()));
        when(fileStorageService.storeFile(eq(file), eq("profile"))).thenReturn("/uploads/signup.jpg");
        when(formatUtils.toPhone("01012341234")).thenReturn("010-1234-1234");
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userWriteTxService.signup(eq(request), eq("010-1234-1234"), eq("/uploads/signup.jpg"), eq("encoded-password"), any()))
            .thenThrow(new RuntimeException("db fail"));

        assertThrows(RuntimeException.class, () -> userService.signup(request));
        verify(fileDeleteTaskService).deleteNowOrEnqueue("/uploads/signup.jpg", "SIGNUP_TX_ROLLBACK");
    }
}
