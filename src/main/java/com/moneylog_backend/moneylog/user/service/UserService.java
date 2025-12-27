package com.moneylog_backend.moneylog.user.service;

import com.moneylog_backend.global.auth.jwt.JwtProvider;
import com.moneylog_backend.global.util.RedisService;
import com.moneylog_backend.moneylog.user.dto.UserDto;
import com.moneylog_backend.moneylog.user.entity.UserEntity;
import com.moneylog_backend.moneylog.user.mapper.UserMapper;
import com.moneylog_backend.moneylog.user.repository.UserRepository;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;

    @Transactional
    public int signup(UserDto userDto){
        if (userRepository.existsByEmail(userDto.getEmail())){
            throw new RuntimeException("이미 가입된 이메일입니다.");
        } // if end

        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        UserEntity userEntity = UserEntity.builder()
                .name(userDto.getName())
                .password(encodedPassword)
                .email(userDto.getEmail())
                .phone(userDto.getPhone() == null ? "" : userDto.getPhone())
                .gender(userDto.isGender())
                .role(userDto.getRole())
                .profile_image_url(userDto.getProfile_image_url() == null ? "" : userDto.getProfile_image_url())
                .build();
        return userRepository.save(userEntity).getUser_id();
    } // func end
} // class end