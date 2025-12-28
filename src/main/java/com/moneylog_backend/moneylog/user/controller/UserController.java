package com.moneylog_backend.moneylog.user.controller;

import com.moneylog_backend.moneylog.user.dto.TokenResponse;
import com.moneylog_backend.moneylog.user.dto.UserDto;
import com.moneylog_backend.moneylog.user.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Integer> signup(@RequestBody UserDto userDto){
        return ResponseEntity.ok(userService.signup(userDto));
    } // func end

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody UserDto userDto){
        return ResponseEntity.ok(userService.login(userDto));
    } // func end

    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(@RequestHeader("Authorization") String authHeader,
                                          Authentication authentication){
        // 순수 토큰값 추출
        String accessToken = authHeader.substring(7);
        // 현재 로그인된 유저 ID 추출
        String userId = authentication.getName();
        // 로그아웃 진행
        userService.logout(accessToken, userId);
        return ResponseEntity.ok(true);
    } // func end
} // class end