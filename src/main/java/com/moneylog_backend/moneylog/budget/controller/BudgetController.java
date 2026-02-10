package com.moneylog_backend.moneylog.budget.controller;

import com.moneylog_backend.global.auth.annotation.LoginUser;
import com.moneylog_backend.moneylog.budget.dto.req.BudgetReqDto;
import com.moneylog_backend.moneylog.budget.dto.res.BudgetResDto;
import com.moneylog_backend.moneylog.budget.service.BudgetService;

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
@RequestMapping("/api/budget")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<?> saveBudget(@RequestBody @Valid BudgetReqDto budgetReqDto, @LoginUser Integer userId) {
        int resultValue = budgetService.saveBudget(budgetReqDto, userId);
        if (resultValue == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            return ResponseEntity.ok(resultValue);
        }
    }

    @GetMapping
    public ResponseEntity<?> getBudgets(@LoginUser Integer userId) {
        return ResponseEntity.ok(budgetService.getBudgets(userId));
    }

    @PutMapping
    public ResponseEntity<?> updateBudget(@RequestBody @Valid BudgetReqDto budgetReqDto, @LoginUser Integer userId) {
        BudgetResDto result = budgetService.updateBudget(budgetReqDto, userId);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            return ResponseEntity.ok(result);
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteBudget(@RequestParam int budgetId, @LoginUser Integer userId) {
        return ResponseEntity.ok(budgetService.deleteBudget(budgetId, userId));
    }
}