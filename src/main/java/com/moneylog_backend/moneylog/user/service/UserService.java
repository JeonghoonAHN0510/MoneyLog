package com.moneylog_backend.moneylog.user.service;

import com.moneylog_backend.global.auth.jwt.JwtProvider;
import com.moneylog_backend.global.file.FileStore;
import com.moneylog_backend.global.util.FormatUtils;
import com.moneylog_backend.global.util.RedisService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;
    private final FileStore fileStore;

    @Transactional
    public int signup (UserDto userDto) throws IOException {
        checkIdOrEmailValidity(userDto);

        userDto.setPhone(FormatUtils.toPhone(userDto.getPhone()));

        userDto.setProfile_image_url(fileStore.storeFile(userDto.getUpload_file()));

        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        userDto.setPassword(encodedPassword);
        // todo 추후 toEntity로 변경(AccountRepository 생성 후) -> 대표 계좌는 회원가입할 때 무조건 받자 그냥 ㅋㅋ
        UserEntity userEntity = userDto.toEntity();
        return userRepository.save(userEntity).getUser_id();
    }

    public TokenResponse login (UserDto userDto) {
        // 1. 인증 객체 생성
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
            userDto.getId(), userDto.getPassword());
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
        Optional<UserEntity> userEntityOptional = userRepository.findByLoginId(loginId);
        if (userEntityOptional.isPresent()) {
            UserEntity userEntity = userEntityOptional.get();
            userEntity.setPassword(null);
            return userEntity.toDto();
        }
        return null;
    }

    public String getUserId (int user_id) {
        Optional<UserEntity> userEntityOptional = userRepository.findById(user_id);
        if (userEntityOptional.isPresent()) {
            UserEntity userEntity = userEntityOptional.get();
            return userEntity.getLoginId();
        }
        return null;
    }

    public void checkIdOrEmailValidity (UserDto userDto) {
        if (userRepository.existsByLoginId(userDto.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 아이디입니다.");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
        }
    }
}