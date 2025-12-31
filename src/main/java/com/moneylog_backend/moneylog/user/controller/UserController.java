package com.moneylog_backend.moneylog.user.controller;

import com.moneylog_backend.moneylog.user.dto.UserDto;
import com.moneylog_backend.moneylog.user.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@ModelAttribute UserDto userDto) throws IOException {
        return ResponseEntity.ok(userService.signup(userDto));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto userDto){
        return ResponseEntity.ok(userService.login(userDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader,
                                          Authentication authentication){
        // 순수 토큰값 추출
        String accessToken = authHeader.substring(7);
        // 현재 로그인된 유저 ID 추출
        String userId = authentication.getName();
        // 로그아웃 진행
        userService.logout(accessToken, userId);
        return ResponseEntity.ok(true);
    }
}