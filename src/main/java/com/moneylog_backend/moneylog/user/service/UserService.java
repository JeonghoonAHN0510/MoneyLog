package com.moneylog_backend.moneylog.user.service;

import com.moneylog_backend.global.auth.jwt.JwtProvider;
import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.exception.ResourceNotFoundException;
import com.moneylog_backend.global.file.FileStorageService;
import com.moneylog_backend.global.file.cleanup.FileDeleteTaskService;
import com.moneylog_backend.global.util.BankAccountNumberFormatter;
import com.moneylog_backend.global.util.FormatUtils;
import com.moneylog_backend.global.util.RedisService;
import com.moneylog_backend.moneylog.bank.entity.BankEntity;
import com.moneylog_backend.moneylog.bank.repository.BankRepository;
import com.moneylog_backend.moneylog.user.dto.LoginReqDto;
import com.moneylog_backend.moneylog.user.dto.TokenResponse;
import com.moneylog_backend.moneylog.user.dto.UserDto;
import com.moneylog_backend.moneylog.user.entity.UserEntity;
import com.moneylog_backend.moneylog.user.mapper.UserMapper;
import com.moneylog_backend.moneylog.user.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Duration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private static final String PROFILE_REPLACED_REASON = "PROFILE_IMAGE_REPLACED";
    private static final String PROFILE_TX_ROLLBACK_REASON = "PROFILE_IMAGE_TX_ROLLBACK";
    private static final String SIGNUP_TX_ROLLBACK_REASON = "SIGNUP_TX_ROLLBACK";

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;
    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final FormatUtils formatUtils;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;
    private final UserWriteTxService userWriteTxService;
    private final FileDeleteTaskService fileDeleteTaskService;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public int signup(UserDto userDto) throws IOException {
        checkIdOrEmailValidity(userDto);

        int bankId = userDto.getBankId();
        String bankName = getBankName(bankId);
        String regexAccountNumber = BankAccountNumberFormatter.format(bankName, userDto.getAccountNumber());
        String profileImageUrl = fileStorageService.storeFile(userDto.getUploadFile(), "profile");
        String regexPhone = formatUtils.toPhone(userDto.getPhone());
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());

        try {
            return userWriteTxService.signup(userDto, regexPhone, profileImageUrl, encodedPassword, regexAccountNumber);
        } catch (RuntimeException ex) {
            fileDeleteTaskService.deleteNowOrEnqueue(profileImageUrl, SIGNUP_TX_ROLLBACK_REASON);
            throw ex;
        }
    }

    public TokenResponse login(LoginReqDto loginReqDto) {
        // 1. 인증 객체 생성
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
            loginReqDto.getId(), loginReqDto.getPassword());
        // 2. 비밀번호 체크
        Authentication authentication = authenticationManagerBuilder.getObject()
                                                                    .authenticate(usernamePasswordAuthenticationToken);
        // 3. JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshToken = jwtProvider.createRefreshToken(authentication);
        // 4. Redis에 Refresh Token 저장
        // Key: "RT:{아이디}", Value: {리프레시 토큰}, Duration: 14일
        redisService.setValues("RT:" + authentication.getName(), refreshToken, Duration.ofDays(14));
        // 5. 토큰 반환
        return TokenResponse.builder()
                            .grantType("Bearer")
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .accessTokenExpireTime(1800L)
                            .build();
    }

    public void logout (String accessToken, String id) {
        // 1. Access Token의 남은 시간 계산
        Long expiration = jwtProvider.getExpiration(accessToken);
        // 2. 시간이 남았다면, 블랙리스트 등록
        if (expiration > 0) {
            redisService.setValues("BL:" + accessToken, "Logout", Duration.ofMillis(expiration));
        }
        // 3. Refresh Token 삭제
        redisService.deleteValues("RT:" + id);
    }

    public UserDto getUserInfo (String loginId) {
        UserEntity userEntity = userRepository.findByLoginId(loginId)
                                              .orElseThrow(
                                                  () -> new ResourceNotFoundException(ErrorMessageConstants.USER_NOT_FOUND));
        return userEntity.excludePassword();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public UserDto updateProfileImage(String loginId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessageConstants.FILE_REQUIRED);
        }

        String newFileUrl = fileStorageService.storeFile(file, "profile");
        try {
            ProfileImageUpdateTxResult txResult = userWriteTxService.updateProfileImageUrl(loginId, newFileUrl);
            String oldFileUrl = txResult.oldFileUrl();
            if (oldFileUrl != null && !oldFileUrl.isBlank() && !oldFileUrl.equals(newFileUrl)) {
                fileDeleteTaskService.enqueueDelete(oldFileUrl, PROFILE_REPLACED_REASON);
            }
            return txResult.userDto();
        } catch (RuntimeException ex) {
            fileDeleteTaskService.deleteNowOrEnqueue(newFileUrl, PROFILE_TX_ROLLBACK_REASON);
            throw ex;
        }
    }

    private void checkIdOrEmailValidity (UserDto userDto) {
        if (userRepository.existsByLoginId(userDto.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessageConstants.DUPLICATE_LOGIN_ID);
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessageConstants.DUPLICATE_EMAIL);
        }
    }

    private String getBankName (int bankId) {
        BankEntity bankEntity = bankRepository.findById(bankId)
                                              .orElseThrow(
                                                  () -> new ResourceNotFoundException(ErrorMessageConstants.BANK_NOT_FOUND));

        return bankEntity.getName();
    }
}
