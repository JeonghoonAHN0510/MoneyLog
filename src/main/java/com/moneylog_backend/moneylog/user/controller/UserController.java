package com.moneylog_backend.moneylog.user.controller;

import com.moneylog_backend.moneylog.user.dto.UserDto;
import com.moneylog_backend.moneylog.user.service.UserService;

import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> signup (@ModelAttribute UserDto userDto) throws IOException {
        return ResponseEntity.ok(userService.signup(userDto));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.login(userDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout (@RequestHeader("Authorization") String authHeader, Authentication authentication) {
        if (authHeader != null) {
            String accessToken = authHeader.substring(7);
            String userId = authentication.getName();
            // 로그아웃 진행
            userService.logout(accessToken, userId);
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo (Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.");
        }
        String loginId = authentication.getName();
        return ResponseEntity.ok(userService.getUserInfo(loginId));
    }
}