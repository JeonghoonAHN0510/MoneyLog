package com.moneylog_backend.moneylog.account.controller;

import com.moneylog_backend.global.auth.annotation.LoginUser;
import com.moneylog_backend.moneylog.account.dto.AccountDto;
import com.moneylog_backend.moneylog.account.service.AccountService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<?> saveAccount (@RequestBody AccountDto accountDto, @LoginUser Integer user_id) {
        if (accountDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        int resultValue = accountService.saveAccount(accountDto, user_id);
        if (resultValue == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            return ResponseEntity.ok(resultValue);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAccount (@RequestParam int account_id, @LoginUser Integer user_id) {
        if (account_id < 30000 || account_id > 40000) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(accountService.getAccount(account_id, user_id));
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAccounts (@LoginUser Integer user_id) {
        return ResponseEntity.ok(accountService.getAccounts(user_id));
    }

    @PutMapping
    public ResponseEntity<?> updateAccount (@RequestBody AccountDto accountDto, @LoginUser Integer user_id) {
        if (accountDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(accountService.updateAccount(accountDto, user_id));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAccount (@RequestParam int account_id, @LoginUser Integer user_id) {
        return ResponseEntity.ok(accountService.deleteAccount(account_id, user_id));
    }

    @PutMapping("/transfer")
    public ResponseEntity<?> transferAccountBalance (@RequestBody AccountDto accountDto, @LoginUser Integer user_id) {
        return ResponseEntity.ok(accountService.transferAccountBalance(accountDto, user_id));
    }
}