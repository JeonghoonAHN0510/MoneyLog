package com.moneylog_backend.moneylog.user.service;

import com.moneylog_backend.global.auth.jwt.JwtProvider;
import com.moneylog_backend.global.auth.jwt.JwtProperties;
import com.moneylog_backend.global.auth.jwt.RedisTokenKeyResolver;
import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.file.FileStorageService;
import com.moneylog_backend.global.file.cleanup.FileDeleteTaskService;
import com.moneylog_backend.global.security.pii.PiiCryptoService;
import com.moneylog_backend.global.security.redis.RedisSecretProtector;
import com.moneylog_backend.global.util.FormatUtils;
import com.moneylog_backend.global.util.RedisService;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.bank.entity.BankEntity;
import com.moneylog_backend.moneylog.bank.repository.BankRepository;
import com.moneylog_backend.moneylog.user.dto.UserDto;
import com.moneylog_backend.moneylog.user.entity.UserEntity;
import com.moneylog_backend.moneylog.user.mapper.UserMapper;
import com.moneylog_backend.moneylog.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
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
    private AuthenticationManager authenticationManager;
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
    private JwtProperties jwtProperties;
    @Mock
    private RedisTokenKeyResolver redisTokenKeyResolver;
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
    @Mock
    private PiiCryptoService piiCryptoService;
    @Mock
    private RedisSecretProtector redisSecretProtector;

    @InjectMocks
    private UserService userService;

    @Test
    void 로그인시_refreshToken은_해시값으로_저장한다() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "tester",
            "",
            List.of(new SimpleGrantedAuthority("USER"))
        );

        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtProvider.createAccessToken(authentication)).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(authentication)).thenReturn("refresh-token");
        when(redisTokenKeyResolver.refreshToken("tester")).thenReturn("RT:tester");
        when(redisSecretProtector.hashRefreshToken("refresh-token")).thenReturn("refresh-token-hash");
        when(jwtProperties.getRefreshTokenValidityInSeconds()).thenReturn(120L);
        when(jwtProperties.getAccessTokenValidityInSeconds()).thenReturn(60L);

        userService.login(new com.moneylog_backend.moneylog.user.dto.LoginReqDto("tester", "password"));

        verify(redisService).setValues("RT:tester", "refresh-token-hash", Duration.ofSeconds(120));
    }

    @Test
    void refresh시_저장된_refreshToken_해시와_비교한다() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "tester",
            "",
            List.of(new SimpleGrantedAuthority("USER"))
        );

        when(jwtProvider.validateToken("refresh-token")).thenReturn(true);
        when(jwtProvider.getAuthentication("refresh-token")).thenReturn(authentication);
        when(redisTokenKeyResolver.refreshToken("tester")).thenReturn("RT:tester");
        when(redisService.getValues("RT:tester")).thenReturn("saved-refresh-hash");
        when(redisSecretProtector.matchesRefreshToken("refresh-token", "saved-refresh-hash")).thenReturn(true);
        when(jwtProvider.createAccessToken(authentication)).thenReturn("new-access-token");
        when(jwtProvider.createRefreshToken(authentication)).thenReturn("new-refresh-token");
        when(redisSecretProtector.hashRefreshToken("new-refresh-token")).thenReturn("new-refresh-token-hash");
        when(jwtProperties.getRefreshTokenValidityInSeconds()).thenReturn(120L);
        when(jwtProperties.getAccessTokenValidityInSeconds()).thenReturn(60L);

        userService.refresh(new com.moneylog_backend.moneylog.user.dto.RefreshReqDto("refresh-token"));

        verify(redisService).setValues("RT:tester", "new-refresh-token-hash", Duration.ofSeconds(120));
    }

    @Test
    void 로그아웃시_accessToken_블랙리스트는_digest_key를_사용한다() {
        when(jwtProvider.getExpiration("access-token")).thenReturn(1_000L);
        when(redisTokenKeyResolver.blacklist("access-token")).thenReturn("BL:token-digest");
        when(redisTokenKeyResolver.refreshToken("tester")).thenReturn("RT:tester");

        userService.logout("access-token", "tester");

        verify(redisService).setValues("BL:token-digest", "Logout", Duration.ofMillis(1_000L));
        verify(redisService).deleteValues("RT:tester");
    }

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
        when(piiCryptoService.normalizeEmail("tester@moneylog.com")).thenReturn("tester@moneylog.com");
        when(piiCryptoService.hashEmail("tester@moneylog.com")).thenReturn("email-hash");
        when(userRepository.existsByEmailHash("email-hash")).thenReturn(false);
        when(userRepository.findAllByEmailHashIsNullOrderByUserIdAsc()).thenReturn(List.of());
        when(bankRepository.findById(1)).thenReturn(Optional.of(BankEntity.builder().bankId(1).name("한국은행").code("001").build()));
        when(fileStorageService.storeFile(eq(file), eq("profile"))).thenReturn("/uploads/signup.jpg");
        when(formatUtils.toPhone("01012341234")).thenReturn("010-1234-1234");
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userWriteTxService.signup(eq(request), eq("tester@moneylog.com"), eq("email-hash"), eq("010-1234-1234"), eq("/uploads/signup.jpg"), eq("encoded-password"), any()))
            .thenThrow(new RuntimeException("db fail"));

        assertThrows(RuntimeException.class, () -> userService.signup(request));
        verify(fileDeleteTaskService).deleteNowOrEnqueue("/uploads/signup.jpg", "SIGNUP_TX_ROLLBACK");
    }

    @Test
    void null_hash_사용자와_이메일이_같으면_중복_이메일로_막는다() {
        UserDto request = UserDto.builder()
                                 .id("tester")
                                 .email("tester@moneylog.com")
                                 .phone("01012341234")
                                 .password("password")
                                 .build();
        UserEntity legacyUser = UserEntity.builder()
                                          .userId(1)
                                          .email("tester@moneylog.com")
                                          .build();

        when(userRepository.existsByLoginId("tester")).thenReturn(false);
        when(piiCryptoService.normalizeEmail("tester@moneylog.com")).thenReturn("tester@moneylog.com");
        when(piiCryptoService.hashEmail("tester@moneylog.com")).thenReturn("email-hash");
        when(userRepository.existsByEmailHash("email-hash")).thenReturn(false);
        when(userRepository.findAllByEmailHashIsNullOrderByUserIdAsc()).thenReturn(List.of(legacyUser));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.signup(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(ErrorMessageConstants.DUPLICATE_EMAIL, exception.getReason());
    }
}
