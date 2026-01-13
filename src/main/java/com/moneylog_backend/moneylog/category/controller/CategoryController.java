package com.moneylog_backend.moneylog.category.controller;

import com.moneylog_backend.global.util.AuthUtils;
import com.moneylog_backend.moneylog.category.dto.CategoryDto;
import com.moneylog_backend.moneylog.category.service.CategoryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> saveCategory (@RequestBody CategoryDto categoryDto, Authentication authentication) {
        String login_id = authUtils.getLoginId(authentication);
        if (login_id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int resultValue = categoryService.saveCategory(categoryDto, login_id);
        if (resultValue == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            return ResponseEntity.ok(resultValue);
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCategory (@RequestParam int category_id, Authentication authentication) {
        String login_id = authUtils.getLoginId(authentication);
        if (login_id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(categoryService.deleteCategory(category_id, login_id));
    }
}