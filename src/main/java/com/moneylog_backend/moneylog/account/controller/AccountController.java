package com.moneylog_backend.moneylog.account.controller;

import com.moneylog_backend.global.util.AuthUtils;
import com.moneylog_backend.moneylog.account.dto.AccountDto;
import com.moneylog_backend.moneylog.account.service.AccountService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> saveAccount (@RequestBody AccountDto accountDto, Authentication authentication) {
        String login_id = authUtils.getLoginId(authentication);
        if (login_id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (accountDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        int resultValue = accountService.saveAccount(accountDto, login_id);
        if (resultValue == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            return ResponseEntity.ok(resultValue);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAccount (@RequestParam int account_id, Authentication authentication) {
        String login_id = authUtils.getLoginId(authentication);
        if (login_id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (account_id < 30000 || account_id > 40000) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(accountService.getAccount(account_id, login_id));
    }

    @PutMapping
    public ResponseEntity<?> updateAccount (@RequestBody AccountDto accountDto, Authentication authentication) {
        String login_id = authUtils.getLoginId(authentication);
        if (login_id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (accountDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(accountService.updateAccount(accountDto, login_id));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAccount (@RequestParam int account_id, Authentication authentication) {
        String login_id = authUtils.getLoginId(authentication);
        if (login_id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(accountService.deleteAccount(account_id, login_id));
    }

    @PutMapping("/transfer")
    public ResponseEntity<?> transferAccountBalance (@RequestBody AccountDto accountDto, Authentication authentication) {
        String login_id = authUtils.getLoginId(authentication);
        if (login_id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(accountService.transferAccountBalance(accountDto, login_id));
    }
}