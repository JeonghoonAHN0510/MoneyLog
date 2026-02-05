package com.moneylog_backend.moneylog.transaction.controller;

import com.moneylog_backend.global.auth.annotation.LoginUser;
import com.moneylog_backend.moneylog.transaction.dto.TransactionDto;
import com.moneylog_backend.moneylog.transaction.service.TransactionService;

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
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> saveTransaction (@RequestBody TransactionDto transactionDto, @LoginUser Integer userId) {
        if (transactionDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        int resultValue = transactionService.saveTransaction(transactionDto, userId);
        if (resultValue == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            return ResponseEntity.ok(resultValue);
        }
    }

    @GetMapping
    public ResponseEntity<?> getTransactionsByUserId (@LoginUser Integer userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(transactionService.getTransactionsByUserId(userId));
    }

    // todo 검색 조건에 따른 가계부 조회 API 필요

    @PutMapping
    public ResponseEntity<?> updateTransaction (@RequestBody TransactionDto transactionDto, @LoginUser Integer userId) {
        if (transactionDto == null || userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(transactionService.updateTransaction(transactionDto, userId));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteTransaction (@RequestParam Integer TransactionId, @LoginUser Integer userId) {
        if (TransactionId == null || userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        TransactionDto transactionDto = TransactionDto.builder().transactionId(TransactionId).userId(userId).build();

        return ResponseEntity.ok(transactionService.deleteTransaction(transactionDto));
    }
}