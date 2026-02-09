package com.moneylog_backend.moneylog.category.controller;

import com.moneylog_backend.global.auth.annotation.LoginUser;
import com.moneylog_backend.moneylog.category.dto.req.CategoryReqDto;
import com.moneylog_backend.moneylog.category.service.CategoryService;

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
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<?> saveCategory(@RequestBody @Valid CategoryReqDto categoryReqDto, @LoginUser Integer userId) {
        int resultValue = categoryService.saveCategory(categoryReqDto, userId);
        if (resultValue == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            return ResponseEntity.ok(resultValue);
        }
    }

    @GetMapping
    public ResponseEntity<?> getCategoryByUserId(@LoginUser Integer userId) {
        return ResponseEntity.ok(categoryService.getCategoryByUserId(userId));
    }

    @PutMapping
    public ResponseEntity<?> updateCategory(@RequestBody @Valid CategoryReqDto categoryReqDto, @LoginUser Integer userId) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryReqDto, userId));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCategory(@RequestParam int categoryId, @LoginUser Integer userId) {
        return ResponseEntity.ok(categoryService.deleteCategory(categoryId, userId));
    }
}