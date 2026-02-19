package com.moneylog_backend.moneylog.account.controller;

import com.moneylog_backend.global.auth.annotation.LoginUser;
import com.moneylog_backend.moneylog.account.dto.req.AccountReqDto;
import com.moneylog_backend.moneylog.account.service.AccountService;
import com.moneylog_backend.moneylog.transaction.dto.TransferDto;

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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<?> saveAccount(@RequestBody @Valid AccountReqDto accountReqDto, @LoginUser Integer userId) {
        int resultValue = accountService.saveAccount(accountReqDto, userId);
        if (resultValue == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            return ResponseEntity.ok(resultValue);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAccount(@RequestParam int accountId, @LoginUser Integer userId) {
        return ResponseEntity.ok(accountService.getAccount(accountId, userId));
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAccounts(@LoginUser Integer userId) {
        return ResponseEntity.ok(accountService.getAccounts(userId));
    }

    @PutMapping
    public ResponseEntity<?> updateAccount(@RequestBody @Valid AccountReqDto accountReqDto, @LoginUser Integer userId) {
        return ResponseEntity.ok(accountService.updateAccount(accountReqDto, userId));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAccount(@RequestParam int accountId, @LoginUser Integer userId) {
        return ResponseEntity.ok(accountService.deleteAccount(accountId, userId));
    }

    @PutMapping("/transfer")
    public ResponseEntity<?> transferAccountBalance(@RequestBody @Valid TransferDto transferDto, @LoginUser Integer userId) {
        return ResponseEntity.ok(accountService.transferAccountBalance(transferDto, userId));
    }
}
