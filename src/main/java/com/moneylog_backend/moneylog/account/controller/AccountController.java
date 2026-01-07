package com.moneylog_backend.moneylog.account.controller;

import com.moneylog_backend.moneylog.account.dto.AccountDto;
import com.moneylog_backend.moneylog.account.service.AccountService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<?> saveAccount (@RequestBody AccountDto accountDto, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (accountDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String login_id = authentication.getName();
        int resultValue = accountService.saveAccount(accountDto, login_id);
        if (resultValue == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            return ResponseEntity.ok(resultValue);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAccount (@RequestParam int account_id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String login_id = authentication.getName();

        return ResponseEntity.ok(accountService.getAccount(account_id, login_id));
    }
}